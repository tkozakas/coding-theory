package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Arrays;

public class FxUserInterface {
    private final Data data;
    public TextField errorProbabilityTextField;
    public ComboBox<String> inputTypeComboBox;
    public TextField inputTextField;
    public TextField alphabetSizeTextField;
    public TextField encodedBlockTextField;
    public TextField currentBlockTextField;
    public TextField withErrorTextField;
    public TextField correctedBlockTextField;
    public TextField decodedBlockTextField;
    public TextField withoutCodeErrorTextField;
    public TextField decodedTextField;
    public TextField withoutCodingTextField;
    public TextField errorCountTextField;
    public TextField errorPositionTextField;
    public TextField noCodingErrorCountTextField;
    public TextField noCodingErrorPositionTextField;

    public FxUserInterface() {
        this.data = Data.getInstance();
    }

    @FXML
    public void initialize() {
        inputTypeComboBox.getSelectionModel().selectFirst();
        alphabetSizeTextField.setText(alphabetSizeTextField.getText() + data.getQ());
        errorProbabilityTextField.setText(errorProbabilityTextField.getText() + data.getPe());
    }

    @FXML
    private void handleInputTypeSelection() {
        inputTextField.clear();
        String selectedType = inputTypeComboBox.getValue();
        inputTextField.setPromptText(switch (selectedType) {
            case "Vector" -> "Vector (e.g., 1, 0, 1)";
            case "Text" -> "Enter text";
            case "Image" -> handleImageInput();
            default -> "Enter input";
        });
        System.out.println("Selected input type: " + selectedType);
    }

    private String handleImageInput() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(inputTextField.getScene().getWindow());
        if (selectedFile != null) {
            inputTextField.setText(selectedFile.getPath());
        }
        return "Enter image path";
    }

    @FXML
    public void encodeInput() {
        data.encodeBlock();
        encodedBlockTextField.setText(Arrays.toString(data.getEncodedBlock()));
    }

    @FXML
    public void sendEncodedBlock() {
        data.introduceErrors();
        withErrorTextField.setText(Arrays.toString(data.getBlockWithError()));
        withoutCodeErrorTextField.setText(Arrays.toString(data.getBlockWithoutCodeError()));
        errorCountTextField.setText(String.valueOf(data.getErrorCount()));
        errorPositionTextField.setText(Arrays.toString(data.getErrorPositions()));
        noCodingErrorCountTextField.setText(String.valueOf(data.getNoCodingErrorCount()));
        noCodingErrorPositionTextField.setText(Arrays.toString(data.getNoCodingErrorPositions()));
    }

    @FXML
    public void decodeBlock() {
        data.decodeBlock();
        correctedBlockTextField.setText(Arrays.toString(data.getCorrectedBlock()));
        decodedBlockTextField.setText(Arrays.toString(data.getDecodedBlock()));
        decodedTextField.setText(String.valueOf(data.getDecodedString()));
        withoutCodingTextField.setText(String.valueOf(data.getWithoutCodingString()));
    }

    public void setInput() {
        String input = inputTextField.getText();
        if (input.isBlank()) {
            showAlert("Invalid Input", "Please enter a valid input.");
            return;
        }
        data.generateInputBits(inputTypeComboBox.getValue());
        data.nextBlock();
        currentBlockTextField.setText(Arrays.toString(data.getBlock()));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setAlphabetSize() {
        String alphabetSize = alphabetSizeTextField.getText();
        if (alphabetSize.isBlank()) {
            showAlert("Invalid Input", "Please enter a valid alphabet size.");
            return;
        }
        data.setQ(Integer.parseInt(alphabetSize));
    }

    public void setProbability() {
        String probability = errorProbabilityTextField.getText();
        if (probability.isBlank()) {
            showAlert("Invalid Input", "Please enter a valid error probability.");
            return;
        }
        data.setPe(Double.parseDouble(probability));
    }
}
