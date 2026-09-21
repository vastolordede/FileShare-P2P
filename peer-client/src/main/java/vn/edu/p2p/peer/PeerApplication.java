package vn.edu.p2p.peer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class PeerApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                PeerApplication.class.getResource(
                        "/vn/edu/p2p/peer/view/login-view.fxml"
                )
        );

        Parent root = loader.load();

        Scene scene = new Scene(root, 460, 420);
        var css = PeerApplication.class.getResource(
                "/vn/edu/p2p/peer/view/styles.css"
        );
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("FileShare-P2P");
        stage.setMinWidth(460);
        stage.setMinHeight(420);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
