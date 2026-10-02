package vn.edu.p2p.peer.share;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import vn.edu.p2p.common.dto.FileMetadata;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for the Share File screen (T01).
 *
 * Same pattern as LoginController: FXML-injected fields, a single-thread
 * background executor for blocking I/O (reading + hashing a file must not
 * run on the JavaFX Application Thread), and Platform.runLater to push
 * results back to the UI.
 */
public final class ShareController {

    @FXML
    private Button chooseFileButton;

    @FXML
    private Button shareButton;

    @FXML
    private Label selectedFileLabel;

    @FXML
    private ListView<String> sharedFilesListView;

    @FXML
    private Label statusLabel;

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "peer-share-io");
        thread.setDaemon(true);
        return thread;
    });

    private final FileMetadataBuilder metadataBuilder = new FileMetadataBuilder();

    private Path selectedFile;

    @FXML
    private void initialize() {
        statusLabel.setText("");
    }

    /** T02: choose a file with FileChooser. */
    @FXML
    private void onChooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn file để chia sẻ");

        File file = chooser.showOpenDialog(chooseFileButton.getScene().getWindow());
        if (file == null) {
            return;
        }

        selectedFile = file.toPath();
        selectedFileLabel.setText(file.getName() + "  (" + file.length() + " bytes)");
        shareButton.setDisable(false);
        statusLabel.setText("");
    }

    /** T03-T09: build FileMetadata (name, size, SHA-256, pieces) in the background. */
    @FXML
    private void onShareFile() {
        if (selectedFile == null) {
            return;
        }

        shareButton.setDisable(true);
        statusLabel.setText("Đang đọc và băm file, vui lòng đợi...");

        ioExecutor.submit(() -> {
            try {
                FileMetadata metadata = metadataBuilder.build(selectedFile);
                Platform.runLater(() -> onMetadataReady(metadata));
            } catch (Exception e) {
                Platform.runLater(() -> onError("Lỗi khi xử lý file: " + e.getMessage()));
            }
        });
    }

    private void onMetadataReady(FileMetadata metadata) {
        statusLabel.setText(
                "Đã tạo metadata cho \"" + metadata.fileName() + "\": "
                        + metadata.pieceCount() + " piece, "
                        + "sha256=" + metadata.fileHash().substring(0, 12) + "..."
        );

        sharedFilesListView.getItems().add(metadata.fileName());
        shareButton.setDisable(false);

        // NEXT (T11, giai đoạn 2): gửi ShareFileRequest chứa metadata này
        // lên Tracker qua TrackerConnection, cùng cách AuthClientService
        // gửi LoginRequest hiện nay.
    }

    private void onError(String message) {
        statusLabel.setText(message);
        shareButton.setDisable(false);
    }

    public void shutdown() {
        ioExecutor.shutdownNow();
    }
}
