package vn.edu.p2p.peer.transfer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import vn.edu.p2p.common.dto.TransferHistoryItemDto;
import vn.edu.p2p.peer.network.TrackerConnection;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * S38: JavaFX Controller cho màn hình Lịch sử truyền file (Transfer History screen).
 */
public final class TransferHistoryController {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    @FXML
    private Button refreshButton;

    @FXML
    private ListView<TransferHistoryItemDto> historyListView;

    @FXML
    private Label statusLabel;

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "peer-history-io");
        thread.setDaemon(true);
        return thread;
    });

    private TransferHistoryClientService historyClientService;
    private String sessionId;

    @FXML
    private void initialize() {
        historyListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(TransferHistoryItemDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                String started = item.startedAtEpochMillis() > 0
                        ? TIME_FMT.format(Instant.ofEpochMilli(item.startedAtEpochMillis()))
                        : "-";
                String details = String.format(
                        "[%s] %s (%d / %d bytes) • %d nguồn Peer • Bắt đầu: %s",
                        item.status(),
                        item.fileName(),
                        item.bytesReceived(),
                        item.fileSize(),
                        item.sourceCount(),
                        started
                );
                if (item.failureReason() != null && !item.failureReason().isBlank()) {
                    details += " • Lý do: " + item.failureReason();
                }
                setText(details);
            }
        });
    }

    public void configure(TrackerConnection connection, String sessionId) {
        this.historyClientService = new TransferHistoryClientService(connection);
        this.sessionId = sessionId;
    }

    @FXML
    public void onRefreshHistory() {
        if (historyClientService == null || sessionId == null) {
            statusLabel.setText("Chưa cấu hình phiên đăng nhập cho màn hình Lịch sử.");
            return;
        }

        refreshButton.setDisable(true);
        statusLabel.setText("Đang tải lịch sử truyền file từ Tracker...");

        ioExecutor.submit(() -> {
            try {
                List<TransferHistoryItemDto> items = historyClientService.fetchHistory(sessionId);
                Platform.runLater(() -> {
                    historyListView.getItems().setAll(items);
                    statusLabel.setText(items.isEmpty()
                            ? "Chưa có lịch sử truyền file nào."
                            : "Đã tải " + items.size() + " phiên truyền file.");
                    refreshButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Lỗi tải lịch sử: " + e.getMessage());
                    refreshButton.setDisable(false);
                });
            }
        });
    }

    public void shutdown() {
        ioExecutor.shutdownNow();
    }
}