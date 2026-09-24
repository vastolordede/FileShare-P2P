package vn.edu.p2p.tracker.network;

import java.io.IOException;
import java.net.ServerSocket;

@FunctionalInterface
public interface TrackerServerSocketProvider {
    ServerSocket open(int port) throws IOException;

    static TrackerServerSocketProvider plain() {
        return ServerSocket::new;
    }
}
