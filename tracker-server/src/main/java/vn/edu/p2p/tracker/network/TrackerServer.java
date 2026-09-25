package vn.edu.p2p.tracker.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class TrackerServer implements AutoCloseable {
    public static final int DEFAULT_WORKER_THREADS = 16;
    public static final int DEFAULT_QUEUE_CAPACITY = 128;
    public static final int DEFAULT_READ_TIMEOUT_MILLIS = 30_000;

    private final int port;
    private final TrackerRequestDispatcher dispatcher;
    private final int workerThreads;
    private final int queueCapacity;
    private final int readTimeoutMillis;
    private final TrackerServerSocketProvider socketProvider;
    private final String transportName;
    private final ThreadPoolExecutor clientPool;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile ServerSocket serverSocket;

    public TrackerServer(int port, TrackerRequestDispatcher dispatcher) {
        this(
                port,
                dispatcher,
                DEFAULT_WORKER_THREADS,
                DEFAULT_QUEUE_CAPACITY,
                DEFAULT_READ_TIMEOUT_MILLIS,
                TrackerServerSocketProvider.plain(),
                "TCP"
        );
    }

    public TrackerServer(
            int port,
            TrackerRequestDispatcher dispatcher,
            int workerThreads
    ) {
        this(
                port,
                dispatcher,
                workerThreads,
                DEFAULT_QUEUE_CAPACITY,
                DEFAULT_READ_TIMEOUT_MILLIS,
                TrackerServerSocketProvider.plain(),
                "TCP"
        );
    }

    public TrackerServer(
            int port,
            TrackerRequestDispatcher dispatcher,
            int workerThreads,
            TrackerServerSocketProvider socketProvider,
            String transportName
    ) {
        this(
                port,
                dispatcher,
                workerThreads,
                DEFAULT_QUEUE_CAPACITY,
                DEFAULT_READ_TIMEOUT_MILLIS,
                socketProvider,
                transportName
        );
    }

    public TrackerServer(
            int port,
            TrackerRequestDispatcher dispatcher,
            int workerThreads,
            int queueCapacity,
            int readTimeoutMillis,
            TrackerServerSocketProvider socketProvider,
            String transportName
    ) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid TCP port: " + port);
        }
        if (workerThreads < 1) {
            throw new IllegalArgumentException("workerThreads must be positive");
        }
        if (queueCapacity < 1) {
            throw new IllegalArgumentException("queueCapacity must be positive");
        }
        if (readTimeoutMillis < 1) {
            throw new IllegalArgumentException("readTimeoutMillis must be positive");
        }

        this.port = port;
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        this.workerThreads = workerThreads;
        this.queueCapacity = queueCapacity;
        this.readTimeoutMillis = readTimeoutMillis;
        this.socketProvider = Objects.requireNonNull(socketProvider, "socketProvider");
        this.transportName = transportName == null || transportName.isBlank()
                ? "TCP"
                : transportName;
        this.clientPool = new ThreadPoolExecutor(
                workerThreads,
                workerThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                new TrackerThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public int port() {
        return port;
    }

    public int activeWorkers() {
        return clientPool.getActiveCount();
    }

    public int queuedConnections() {
        return clientPool.getQueue().size();
    }

    public void start() throws IOException {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("TrackerServer is already running");
        }

        try (ServerSocket server = socketProvider.open(port)) {
            serverSocket = server;
            System.out.printf(
                    "Tracker %s server listening on 0.0.0.0:%d with %d workers, queue=%d, readTimeout=%dms%n",
                    transportName,
                    port,
                    workerThreads,
                    queueCapacity,
                    readTimeoutMillis
            );

            while (running.get()) {
                try {
                    Socket client = server.accept();
                    submit(client);
                } catch (SocketException e) {
                    if (running.get()) {
                        throw e;
                    }
                }
            }
        } finally {
            running.set(false);
            serverSocket = null;
        }
    }

    private void submit(Socket client) {
        try {
            clientPool.execute(
                    new ClientHandler(client, dispatcher, readTimeoutMillis)
            );
        } catch (RejectedExecutionException e) {
            System.err.printf(
                    "Tracker overloaded; rejecting connection from %s.%n",
                    client.getRemoteSocketAddress()
            );
            try {
                client.close();
            } catch (IOException ignored) {
                // best effort
            }
        }
    }

    @Override
    public void close() {
        running.set(false);

        ServerSocket server = serverSocket;
        if (server != null) {
            try {
                server.close();
            } catch (IOException ignored) {
                // best effort during shutdown
            }
        }

        clientPool.shutdownNow();
    }

    private static final class TrackerThreadFactory implements ThreadFactory {
        private final AtomicInteger sequence = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(
                    runnable,
                    "tracker-client-" + sequence.incrementAndGet()
            );
            thread.setDaemon(true);
            return thread;
        }
    }
}
