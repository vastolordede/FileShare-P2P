package vn.edu.p2p.peer.auth;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import vn.edu.p2p.peer.network.PeerSettings;
import vn.edu.p2p.peer.network.TrackerConnection;
import vn.edu.p2p.peer.session.ClientSession;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField listeningPortField;

    @FXML
    private Button loginButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Label statusLabel;

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "peer-auth-io");
        thread.setDaemon(true);
        return thread;
    });

    private AuthClientService authClientService;
    private String initializationError;

    @FXML
    private void initialize() {
        listeningPortField.setText("7001");
        logoutButton.setDisable(true);

        try {
            PeerSettings settings = PeerSettings.load();
            PeerIdentity identity = PeerIdentityStore.defaultStore().loadOrCreate();
            TrackerConnection connection = new TrackerConnection(
                    settings.trackerConnectionConfig()
            );
            authClientService = new AuthClientService(connection, identity);

            statusLabel.setText(
                    "Tracker: " + settings.trackerHost() + ":" + settings.trackerPort()
            );
        } catch (Exception e) {
            initializationError = e.getMessage();
            loginButton.setDisable(true);
            statusLabel.setText("Không thể khởi tạo Peer: " + e.getMessage());
        }
    }

    @FXML
    private void onLogin() {
        if (initializationError != null || authClientService == null) {
            statusLabel.setText("Peer chưa sẵn sàng: " + initializationError);
            return;
        }

        String error = LoginFormValidator.validate(
                usernameField.getText(),
                passwordField.getText(),
                listeningPortField.getText()
        );
        if (error != null) {
            statusLabel.setText(error);
            return;
        }

        int listeningPort = Integer.parseInt(listeningPortField.getText());
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        setBusy(true);
        statusLabel.setText("Đang kết nối Tracker...");

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return authClientService.login(
                                username,
                                password,
                                listeningPort
                        );
                    } catch (AuthClientException e) {
                        throw new AuthOperationRuntimeException(e);
                    }
                }, ioExecutor)
                .whenComplete((session, throwable) -> Platform.runLater(() -> {
                    setBusy(false);

                    if (throwable != null) {
                        Throwable cause = unwrap(throwable);
                        statusLabel.setText("Đăng nhập thất bại: " + cause.getMessage());
                        return;
                    }

                    onLoginSuccess(session);
                }));
    }

    @FXML
    private void onLogout() {
        if (authClientService == null || authClientService.currentSession() == null) {
            return;
        }

        setBusy(true);
        statusLabel.setText("Đang đăng xuất...");

        CompletableFuture
                .runAsync(() -> {
                    try {
                        authClientService.logout();
                    } catch (AuthClientException e) {
                        throw new AuthOperationRuntimeException(e);
                    }
                }, ioExecutor)
                .whenComplete((ignored, throwable) -> Platform.runLater(() -> {
                    setBusy(false);

                    if (throwable != null) {
                        Throwable cause = unwrap(throwable);
                        statusLabel.setText("Đăng xuất thất bại: " + cause.getMessage());
                        return;
                    }

                    usernameField.setDisable(false);
                    passwordField.setDisable(false);
                    listeningPortField.setDisable(false);
                    loginButton.setDisable(false);
                    logoutButton.setDisable(true);
                    statusLabel.setText("Đã đăng xuất khỏi Tracker.");
                }));
    }

    private void onLoginSuccess(ClientSession session) {
        passwordField.clear();
        usernameField.setDisable(true);
        passwordField.setDisable(true);
        listeningPortField.setDisable(true);
        loginButton.setDisable(true);
        logoutButton.setDisable(false);
        statusLabel.setText(
                "Đăng nhập thành công. Peer=" + shortId(session.peerId())
                        + " | Session=" + shortId(session.sessionId())
        );
    }

    private void setBusy(boolean busy) {
        if (busy) {
            loginButton.setDisable(true);
            logoutButton.setDisable(true);
        } else if (authClientService != null && authClientService.currentSession() != null) {
            loginButton.setDisable(true);
            logoutButton.setDisable(false);
        } else {
            loginButton.setDisable(false);
            logoutButton.setDisable(true);
        }
    }

    public void shutdown() {
        if (authClientService != null) {
            authClientService.close();
        }
        ioExecutor.shutdownNow();
    }

    private static Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null
                && (current instanceof java.util.concurrent.CompletionException
                || current instanceof AuthOperationRuntimeException)) {
            current = current.getCause();
        }
        return current;
    }

    private static String shortId(String id) {
        return id == null || id.length() <= 8 ? id : id.substring(0, 8);
    }

    private static final class AuthOperationRuntimeException extends RuntimeException {
        AuthOperationRuntimeException(Throwable cause) {
            super(cause);
        }
    }
}
