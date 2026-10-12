package vn.edu.p2p.peer.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.edu.p2p.common.dto.FileMetadata;
import vn.edu.p2p.peer.share.FileMetadataBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * T32: duplicate-file detection on the Tracker (T13) keys off FileMetadata.fileHash,
 * so this hash MUST match for identical content and differ for different content -
 * regardless of the file's name.
 */
class FileMetadataBuilderTest {

    @Test
    void twoFilesWithIdenticalContentButDifferentNames_produceTheSameHash(@TempDir Path dir) throws Exception {
        byte[] content = randomBytes(5000);

        Path fileA = dir.resolve("report-v1.pdf");
        Path fileB = dir.resolve("report-final-FINAL.pdf");
        Files.write(fileA, content);
        Files.write(fileB, content);

        FileMetadataBuilder builder = new FileMetadataBuilder(1024);
        FileMetadata metadataA = builder.build(fileA);
        FileMetadata metadataB = builder.build(fileB);

        assertEquals(
                metadataA.fileHash(), metadataB.fileHash(),
                "identical content must hash the same regardless of file name, "
                        + "so the Tracker's T13 duplicate check can key off fileHash alone"
        );
        assertEquals(metadataA.pieceCount(), metadataB.pieceCount());
        assertNotEquals(metadataA.fileName(), metadataB.fileName());
    }

    @Test
    void oneByteDifference_producesADifferentHash(@TempDir Path dir) throws Exception {
        byte[] contentA = randomBytes(500);
        byte[] contentB = contentA.clone();
        contentB[0] ^= 0x01; // flip one bit -> must not be treated as a duplicate

        Path fileA = dir.resolve("a.bin");
        Path fileB = dir.resolve("b.bin");
        Files.write(fileA, contentA);
        Files.write(fileB, contentB);

        FileMetadataBuilder builder = new FileMetadataBuilder(1024);
        FileMetadata metadataA = builder.build(fileA);
        FileMetadata metadataB = builder.build(fileB);

        assertNotEquals(metadataA.fileHash(), metadataB.fileHash());
    }

    private static byte[] randomBytes(int length) {
        byte[] data = new byte[length];
        new Random(7).nextBytes(data);
        return data;
    }
}
