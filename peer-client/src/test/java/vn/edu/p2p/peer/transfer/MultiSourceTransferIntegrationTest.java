package vn.edu.p2p.peer.transfer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.edu.p2p.common.dto.FileMetadata;
import vn.edu.p2p.common.dto.FileSourceInfo;
import vn.edu.p2p.peer.share.FileHashUtil;
import vn.edu.p2p.peer.share.FileMetadataBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiSourceTransferIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDownloadFileConcurrentlyFromTwoPeersAndVerifySha256() throws Exception {
        // Tạo file mẫu 700 KB (với pieceSize = 256 KB sẽ chia thành 3 pieces: 0, 1, 2)
        Path originalFile = tempDir.resolve("sample-video.bin");
        byte[] content = new byte[700 * 1024];
        new Random(42).nextBytes(content);
        Files.write(originalFile, content);

        FileMetadata metadata = new FileMetadataBuilder().build(originalFile);
        long fileId = 101L;

        PeerP2pSocketFactory socketFactory = new PeerP2pSocketFactory(PeerP2pSocketConfig.plainTcp());

        // Peer A chỉ giữ Piece 0 và Piece 1 (PARTIAL)
        LocalFileRegistry registryA = new LocalFileRegistry();
        BitSet piecesA = new BitSet(3);
        piecesA.set(0);
        piecesA.set(1);
        Path fileA = tempDir.resolve("peerA.bin");
        Files.copy(originalFile, fileA);
        registryA.registerDownloadingFile(fileId, fileA, metadata, piecesA);

        // Peer B giữ Piece 1 và Piece 2 (PARTIAL)
        LocalFileRegistry registryB = new LocalFileRegistry();
        BitSet piecesB = new BitSet(3);
        piecesB.set(1);
        piecesB.set(2);
        Path fileB = tempDir.resolve("peerB.bin");
        Files.copy(originalFile, fileB);
        registryB.registerDownloadingFile(fileId, fileB, metadata, piecesB);

        try (PeerUploadServer serverA = new PeerUploadServer(0, registryA, socketFactory);
             PeerUploadServer serverB = new PeerUploadServer(0, registryB, socketFactory)) {

            serverA.start();
            serverB.start();

            List<FileSourceInfo> sources = List.of(
                    new FileSourceInfo("peer-A", "PARTIAL", "127.0.0.1", serverA.listeningPort(), System.currentTimeMillis()),
                    new FileSourceInfo("peer-B", "PARTIAL", "127.0.0.1", serverB.listeningPort(), System.currentTimeMillis())
            );

            // Peer C tải đồng thời từ Peer A + Peer B
            LocalFileRegistry registryC = new LocalFileRegistry();
            PeerDownloadClient downloadClient = new PeerDownloadClient(socketFactory);
            MultiSourceDownloadEngine engine = new MultiSourceDownloadEngine(
                    registryC,
                    downloadClient,
                    4,
                    PieceScheduler.Strategy.RAREST_FIRST
            );

            Path downloadedFile = tempDir.resolve("peerC-downloaded.bin");
            DownloadSessionResult result = engine.downloadFile(
                    fileId,
                    sources,
                    downloadedFile,
                    null,
                    new AtomicBoolean(false)
            );

            assertEquals("COMPLETED", result.status());
            assertEquals(content.length, result.bytesReceived());
            assertEquals(metadata.fileHash(), FileHashUtil.sha256(downloadedFile));
            assertTrue(registryC.find(fileId).orElseThrow().isComplete());
        }
    }
}