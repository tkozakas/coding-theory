import javafx.application.Application;
import ui.UserInterface;

public class Launcher {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("ui")) {
            Application.launch(App.class, args);
        } else {
            new UserInterface().start();
        }
    }
}
