package vn.edu.p2p.tracker.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class TrackerServer implements AutoCloseable {
    public static final int DEFAULT_WORKER_THREADS = 16;

    private final int port;
    private final TrackerRequestDispatcher dispatcher;
    private final int workerThreads;
    private final ExecutorService clientPool;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile ServerSocket serverSocket;

    public TrackerServer(int port, TrackerRequestDispatcher dispatcher) {
        this(port, dispatcher, DEFAULT_WORKER_THREADS);
    }

    public TrackerServer(
            int port,
            TrackerRequestDispatcher dispatcher,
            int workerThreads
    ) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid TCP port: " + port);
        }
        if (workerThreads < 1) {
            throw new IllegalArgumentException("workerThreads must be positive");
        }
        this.port = port;
        this.dispatcher = dispatcher;
        this.workerThreads = workerThreads;
        this.clientPool = Executors.newFixedThreadPool(
                workerThreads,
                new TrackerThreadFactory()
        );
    }

    public int port() {
        return port;
    }

    public void start() throws IOException {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("TrackerServer is already running");
        }

        try (ServerSocket server = new ServerSocket(port)) {
            serverSocket = server;
            System.out.printf(
                    "Tracker TCP server listening on 0.0.0.0:%d with %d workers%n",
                    port,
                    workerThreads
            );

            while (running.get()) {
                try {
                    Socket client = server.accept();
                    clientPool.submit(new ClientHandler(client, dispatcher));
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
