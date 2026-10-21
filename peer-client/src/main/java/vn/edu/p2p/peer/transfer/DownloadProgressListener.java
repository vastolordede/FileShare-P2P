package vn.edu.p2p.peer.transfer;

/**
 * S16, S39: Listener nhận thông báo tiến độ tải file và tốc độ download (bytes/sec).
 */
public interface DownloadProgressListener {

    void onProgress(
            long fileId,
            int completedPieces,
            int totalPieces,
            long bytesReceived,
            long totalBytes,
            double downloadSpeedBytesPerSec
    );

    default void onSourceStatusChanged(String peerId, String status, String message) {
    }
}