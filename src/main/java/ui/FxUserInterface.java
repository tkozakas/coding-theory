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
    public TextField fixedCountTextFiled;
    public TextField fixedPositionTextField;
    public TextField noCodingFixedCountTextField;
    public TextField noCodingFixedPositionTextField;

    public FxUserInterface() {
        this.data = Data.getInstance();
    }

    @FXML
    public void initialize() {
        inputTypeComboBox.getSelectionModel().selectFirst();
        alphabetSizeTextField.setText(alphabetSizeTextField.getText() + data.getQ());
        errorProbabilityTextField.setText(errorProbabilityTextField.getText() + data.getPe());
        addListeners();
    }

    private void addListeners() {
        errorProbabilityTextField.textProperty().addListener((observable, oldValue, newValue) -> updateProbability(newValue));
        alphabetSizeTextField.textProperty().addListener((observable, oldValue, newValue) -> updateAlphabetSize(newValue));
        withErrorTextField.textProperty().addListener((observable, oldValue, newValue) -> updateWithErrorBlock(newValue));
        withoutCodeErrorTextField.textProperty().addListener((observable, oldValue, newValue) -> updateWithoutCodeErrorBlock(newValue));
    }

    private int[] parseStringToBits(String newValue) {
        return Arrays.stream(newValue.split("[\\[\\], ]+"))
                .filter(s -> !s.isBlank())
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private void updateWithoutCodeErrorBlock(String newValue) {
        if (newValue.isBlank()) {
            return;
        }
        data.setBlockWithoutCodeError(parseStringToBits(newValue));
    }

    private void updateWithErrorBlock(String newValue) {
        if (newValue.isBlank()) {
            return;
        }
        data.setBlockWithError(parseStringToBits(newValue));
    }

    private void updateAlphabetSize(String newValue) {
        if (newValue.isBlank()) {
            showAlert("Please enter a valid alphabet size.");
            return;
        }
        data.setQ(Integer.parseInt(newValue));
    }

    private void updateProbability(String newValue) {
        if (newValue.isBlank()) {
            showAlert("Please enter a valid error probability.");
            return;
        }
        data.setPe(Double.parseDouble(newValue));
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
        data.setBlockWithError(parseStringToBits(withErrorTextField.getText()));
        data.setBlockWithoutCodeError(parseStringToBits(withoutCodeErrorTextField.getText()));
        errorCountTextField.setText(String.valueOf(data.getErrorCount()));
        errorPositionTextField.setText(Arrays.toString(data.getErrorPositions()));
        noCodingErrorCountTextField.setText(String.valueOf(data.getNoCodingErrorCount()));
        noCodingErrorPositionTextField.setText(Arrays.toString(data.getNoCodingErrorPositions()));

        data.decodeBlock();
        correctedBlockTextField.setText(Arrays.toString(data.getCorrectedBlock()));
        decodedBlockTextField.setText(Arrays.toString(data.getDecodedBlock()));
        decodedTextField.setText(String.valueOf(data.getDecodedString()));
        withoutCodingTextField.setText(String.valueOf(data.getWithoutCodingString()));
        fixedCountTextFiled.setText(String.valueOf(data.getFixedCount()));
        fixedPositionTextField.setText(Arrays.toString(data.getFixedPositions()));
        noCodingFixedCountTextField.setText(String.valueOf(data.getNoCodingFixedCount()));
        noCodingFixedPositionTextField.setText(Arrays.toString(data.getNoCodingFixedPositions()));

        if (data.getCurrentBitPosition() >= data.getInputBits().length) {
            showAlert("All blocks have been processed.");
            if (inputTypeComboBox.getValue().equals("Image")) {
                data.writeImage();
            }
        }
    }

    public void setNextBlock() {
        String input = inputTextField.getText();
        if (input.isBlank()) {
            showAlert("Please enter a valid input.");
            return;
        }
        data.generateInputBits(inputTypeComboBox.getValue(), input);
        data.nextBlock();
        currentBlockTextField.setText(Arrays.toString(data.getBlock()));
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void processInput() {
        clearInput();
        do {
            setNextBlock();
            encodeInput();
            sendEncodedBlock();
            decodeBlock();
        } while (data.getCurrentBitPosition() < data.getInputBits().length);
        data.setCurrentBitPosition(0);
    }

    public void clearInput() {
        currentBlockTextField.clear();
        encodedBlockTextField.clear();
        withErrorTextField.clear();
        correctedBlockTextField.clear();
        decodedBlockTextField.clear();
        withoutCodeErrorTextField.clear();
        decodedTextField.clear();
        withoutCodingTextField.clear();
        errorCountTextField.clear();
        errorPositionTextField.clear();
        noCodingErrorCountTextField.clear();
        noCodingErrorPositionTextField.clear();
        fixedCountTextFiled.clear();
        fixedPositionTextField.clear();
        noCodingFixedCountTextField.clear();
        noCodingFixedPositionTextField.clear();
        data.getDecodedBlocks().clear();
        data.getBlocksWithoutCode().clear();
        data.setCurrentBitPosition(0);
    }
}
