import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MainSplitPane.fxml"));
        Parent root = loader.load();
        stage.setTitle("Error Correcting Code");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
