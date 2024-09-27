import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.UserInterface;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MainSplitPane.fxml"));
        Parent root = loader.load();
        stage.setTitle("Error Correcting Code");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("ui")) {
            launch(args);
        }
        new UserInterface().start();
    }
}
