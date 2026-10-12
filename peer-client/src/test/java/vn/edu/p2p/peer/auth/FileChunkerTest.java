package vn.edu.p2p.peer.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.edu.p2p.common.dto.PieceInfo;
import vn.edu.p2p.peer.share.FileChunker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T29-T31: chunking behaviour for a small file, a large file, and a partial last piece. */
class FileChunkerTest {

    private static final int PIECE_SIZE = 1024; // 1 KiB, kept small so tests run fast

    @Test
    void smallFileSmallerThanOnePiece_producesExactlyOnePiece(@TempDir Path dir) throws Exception {
        // T29: "file nhỏ" - content shorter than a single piece.
        Path file = dir.resolve("small.bin");
        Files.write(file, randomBytes(200));

        List<PieceInfo> pieces = FileChunker.chunk(file, PIECE_SIZE);

        assertEquals(1, pieces.size());
        assertEquals(0, pieces.get(0).pieceIndex());
        assertEquals(200, pieces.get(0).pieceLength());
    }

    @Test
    void emptyFile_producesZeroPieces(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("empty.bin");
        Files.write(file, new byte[0]);

        List<PieceInfo> pieces = FileChunker.chunk(file, PIECE_SIZE);

        assertTrue(pieces.isEmpty());
        assertEquals(0, FileChunker.pieceCount(0, PIECE_SIZE));
    }

    @Test
    void largeFile_exactMultipleOfPieceSize_producesEqualSizedPieces(@TempDir Path dir) throws Exception {
        // T30: "file lớn" - several full pieces, no partial last piece.
        Path file = dir.resolve("large.bin");
        int pieceCountExpected = 5;
        Files.write(file, randomBytes(PIECE_SIZE * pieceCountExpected));

        List<PieceInfo> pieces = FileChunker.chunk(file, PIECE_SIZE);

        assertEquals(pieceCountExpected, pieces.size());
        for (PieceInfo piece : pieces) {
            assertEquals(PIECE_SIZE, piece.pieceLength());
        }
    }

    @Test
    void lastPiece_shorterThanPieceSize_isSizedCorrectly(@TempDir Path dir) throws Exception {
        // T31: last piece not an exact multiple of pieceSize.
        Path file = dir.resolve("odd.bin");
        int fullPieces = 3;
        int remainder = 137;
        Files.write(file, randomBytes(PIECE_SIZE * fullPieces + remainder));

        List<PieceInfo> pieces = FileChunker.chunk(file, PIECE_SIZE);

        assertEquals(fullPieces + 1, pieces.size());
        for (int i = 0; i < fullPieces; i++) {
            assertEquals(PIECE_SIZE, pieces.get(i).pieceLength());
        }

        PieceInfo lastPiece = pieces.get(pieces.size() - 1);
        assertEquals(remainder, lastPiece.pieceLength());
        assertEquals(fullPieces, lastPiece.pieceIndex());
    }

    @Test
    void everyPiece_hasA64CharacterHexSha256Hash(@TempDir Path dir) throws Exception {
        // T27: SHA-256 is computed per piece, not just for the whole file.
        Path file = dir.resolve("hashed.bin");
        Files.write(file, randomBytes(PIECE_SIZE * 2 + 50));

        List<PieceInfo> pieces = FileChunker.chunk(file, PIECE_SIZE);

        for (PieceInfo piece : pieces) {
            assertTrue(
                    piece.pieceHash().matches("^[0-9a-f]{64}$"),
                    "piece hash should be a 64-char lowercase hex SHA-256 string"
            );
        }
    }

    private static byte[] randomBytes(int length) {
        byte[] data = new byte[length];
        new Random(42).nextBytes(data);
        return data;
    }
}
