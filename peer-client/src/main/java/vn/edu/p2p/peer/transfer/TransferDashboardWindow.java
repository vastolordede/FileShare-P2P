package vn.edu.p2p.peer.transfer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import vn.edu.p2p.peer.network.TrackerConnection;

import java.io.IOException;

/**
 * Cửa sổ trung tâm Transfer Center của Sang (gồm Tab Download và Tab Lịch sử truyền file),
 * đồng thời khởi chạy ngầm PeerUploadServer tại listeningPort của Peer.
 */
public final class TransferDashboardWindow implements AutoCloseable {

    private final LocalFileRegistry fileRegistry;
    private final PeerP2pSocketFactory socketFactory;
    private final PeerUploadServer uploadServer;
    private DownloadController downloadController;
    private TransferHistoryController historyController;
    private Stage stage;

    public TransferDashboardWindow(int listeningPort, PeerP2pSocketConfig p2pConfig) {
        this.fileRegistry = new LocalFileRegistry();
        this.socketFactory = new PeerP2pSocketFactory(p2pConfig);
        this.uploadServer = new PeerUploadServer(listeningPort, fileRegistry, socketFactory);
    }

    public void show(TrackerConnection connection, String sessionId) throws IOException {
        uploadServer.start();

        FXMLLoader downloadLoader = new FXMLLoader(
                TransferDashboardWindow.class.getResource("/vn/edu/p2p/peer/view/download-view.fxml")
        );
        Parent downloadRoot = downloadLoader.load();
        downloadController = downloadLoader.getController();
        downloadController.configure(connection, sessionId, fileRegistry, socketFactory, uploadServer);

        FXMLLoader historyLoader = new FXMLLoader(
                TransferDashboardWindow.class.getResource("/vn/edu/p2p/peer/view/history-view.fxml")
        );
        Parent historyRoot = historyLoader.load();
        historyController = historyLoader.getController();
        historyController.configure(connection, sessionId);

        Tab downloadTab = new Tab("Tải File (Download)", downloadRoot);
        downloadTab.setClosable(false);

        Tab historyTab = new Tab("Lịch Sử Truyền File (History)", historyRoot);
        historyTab.setClosable(false);
        historyTab.setOnSelectionChanged(event -> {
            if (historyTab.isSelected() && historyController != null) {
                historyController.onRefreshHistory();
            }
        });

        TabPane tabPane = new TabPane(downloadTab, historyTab);
        Scene scene = new Scene(tabPane, 760, 520);

        stage = new Stage();
        stage.setTitle("FileShare-P2P • Transfer Engine (Port: " + uploadServer.listeningPort() + ")");
        stage.setScene(scene);
        stage.setOnHidden(e -> close());
        stage.show();
    }

    public LocalFileRegistry fileRegistry() {
        return fileRegistry;
    }

    public DownloadController downloadController() {
        return downloadController;
    }

    @Override
    public void close() {
        if (downloadController != null) {
            downloadController.shutdown();
        }
        if (historyController != null) {
            historyController.shutdown();
        }
        uploadServer.close();
    }
}