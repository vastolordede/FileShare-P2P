package vn.edu.p2p.peer.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import vn.edu.p2p.common.protocol.MessageEnvelope;
import vn.edu.p2p.common.protocol.ProtocolCodec;

public final class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField listeningPortField;

    @FXML
    private Label statusLabel;

    private final AuthClientService authClientService =
            new AuthClientService();

    @FXML
    private void initialize() {
        listeningPortField.setText("7001");
        statusLabel.setText("Week 1: chưa kết nối Tracker.");
    }

    @FXML
    private void onLogin() {
        String error = LoginFormValidator.validate(
                usernameField.getText(),
                passwordField.getText(),
                listeningPortField.getText()
        );

        if (error != null) {
            statusLabel.setText(error);
            return;
        }

        int listeningPort = Integer.parseInt(
                listeningPortField.getText()
        );

        MessageEnvelope request =
                authClientService.createLoginRequest(
                        usernameField.getText().trim(),
                        passwordField.getText(),
                        listeningPort
                );

        try {
            // Week 1: prove GUI -> DTO -> protocol envelope works.
            // Week 2: replace console output with TrackerConnection.send(...).
            System.out.println(ProtocolCodec.toJson(request));

            statusLabel.setText(
                    "LOGIN_REQUEST đã tạo đúng protocol v1. "
                            + "TCP login sẽ nối ở Week 2."
            );
        } catch (Exception e) {
            statusLabel.setText(
                    "Không thể tạo LOGIN_REQUEST: " + e.getMessage()
            );
        }
    }
}
