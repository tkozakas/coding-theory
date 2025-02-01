import ui.UserInterface;

public class Main {
//    @Override
//    public void start(Stage stage) throws IOException {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MainSplitPane.fxml"));
//        Parent root = loader.load();
//        stage.setTitle("Error Correcting Code");
//        stage.setScene(new Scene(root));
//        stage.show();
//    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("ui")) {
//            launch(args);
        } else {
            new UserInterface().start();
        }
    }
}
