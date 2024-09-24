package ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.util.converter.IntegerStringConverter;
import model.CosetLeader;
import processor.EncoderDecoder;
import processor.ImageProcessor;
import processor.TextProcessor;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FxUserInterface {
    public TableView<Integer[]> generatingMatrixTable;
    public TableView<Integer[]> parityCheckMatrixTable;
    public TableView<CosetLeader> cosetLeaderTable;

    public ComboBox<String> inputTypeComboBox;

    public TextField columnsField;
    public TextField rowsField;
    public TextField totalCosetLeaders;
    public TextField inputField;
    public TextField errorProbabilityField;
    public TextField encodedInputTextField;
    public TextField notEncodedInputTextField;
    public TextField receivedBitsEncodedTextField;
    public TextField receivedNotEncodedTextField;
    public TextField correctedEncodedTextField;
    public TextField correctedNotEncodedTextField;
    public TextField decodedEncodedTextField;
    public TextField decodedNotEncodedTextField;
    public TextField alphabetSizeField;

    public Label errorLabel;
    public Label errorPositionLabel;
    public Label blockLabel;
    public Label probabilityLabel;
    public Label alphabetLabel;

    private boolean debugMode = false;
    private EncoderDecoder encoderDecoder;
    private TextProcessor textProcessor;
    private ImageProcessor imageProcessor;

    private double pe = 0.0001; // Probability of error
    private int q = 2; // Number of symbols in the alphabet

    private int[][] G;
    private int[][] H;
    private int n;
    private int k;

    private int currentBitPosition = 0;

    private int[] encodedBits;
    private int[] notEncodedBits;

    private int[] receivedBitsForEncoded;
    private int[] receivedBitsForNotEncoded;

    @FXML
    private void initialize() {
        inputTypeComboBox.getSelectionModel().selectFirst();
        encoderDecoder = new EncoderDecoder();
        alphabetLabel.setText("Alphabet (q): " + q);
        probabilityLabel.setText("Error Probability (%): " + pe);
    }

    private void updateMatrix() {
        H = encoderDecoder.generateParityCheckMatrix(G);
        setupMatrixTable(parityCheckMatrixTable, H, n - k, n);
        List<CosetLeader> cosetLeaders = encoderDecoder.findCosetLeaders(H);
        setupCosetLeaderTable(cosetLeaders);
    }

    @FXML
    private void handleInputTypeSelection() {
        inputField.clear();
        String selectedType = inputTypeComboBox.getValue();
        inputField.setPromptText(switch (selectedType) {
            case "Vector" -> "Enter vector (e.g., 1 0 1)";
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
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(inputField.getScene().getWindow());
        if (selectedFile != null) {
            inputField.setText(selectedFile.getPath());
        }
        return "Enter image path";
    }

    @FXML
    private void generateMatrix() {
        try {
            n = Integer.parseInt(columnsField.getText());
            k = Integer.parseInt(rowsField.getText());

            if (k > n) {
                showAlert("Invalid Input", "The number of rows (k) should be less than or equal to the number of columns (n).");
                return;
            }

            G = encoderDecoder.generateGeneratingMatrix(k, n);
            setupMatrixTable(generatingMatrixTable, G, k, n);
            H = encoderDecoder.generateParityCheckMatrix(G);
            setupMatrixTable(parityCheckMatrixTable, H, n - k, n);
            List<CosetLeader> cosetLeaders = encoderDecoder.findCosetLeaders(H);
            setupCosetLeaderTable(cosetLeaders);

            textProcessor = new TextProcessor(encoderDecoder, G, k, pe, q);
            imageProcessor = new ImageProcessor(encoderDecoder, G, k, pe, q);

        } catch (NumberFormatException e) {
            showAlert("Invalid input", "Please enter valid integers for n and k.");
        }
    }

    private void setupCosetLeaderTable(List<CosetLeader> cosetLeaders) {
        cosetLeaderTable.getColumns().clear();
        cosetLeaderTable.getItems().clear();

        TableColumn<CosetLeader, String> syndromeColumn = new TableColumn<>("Syndrome");
        syndromeColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(Arrays.toString(cellData.getValue().syndrome())));
        cosetLeaderTable.getColumns().add(syndromeColumn);

        TableColumn<CosetLeader, String> cosetLeaderColumn = new TableColumn<>("Error Pattern");
        cosetLeaderColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(Arrays.toString(cellData.getValue().errorPattern())));
        cosetLeaderTable.getColumns().add(cosetLeaderColumn);

        TableColumn<CosetLeader, String> errorColumn = new TableColumn<>("Weight");
        errorColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().weight()).asString());
        cosetLeaderTable.getColumns().add(errorColumn);

        cosetLeaderTable.getItems().addAll(cosetLeaders);
        totalCosetLeaders.setText(String.valueOf(cosetLeaders.size()));
    }

    private void setupMatrixTable(TableView<Integer[]> table, int[][] matrix, int rows, int columns) {
        table.getColumns().clear();
        table.getItems().clear();
        for (int colIndex = 0; colIndex < columns; colIndex++) {
            TableColumn<Integer[], Integer> column = getTableColumn(table, colIndex);
            table.getColumns().add(column);
        }
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            Integer[] rowData = new Integer[columns];
            for (int colIndex = 0; colIndex < columns; colIndex++) {
                rowData[colIndex] = matrix[rowIndex][colIndex];
            }
            table.getItems().add(rowData);
        }
        table.setEditable(true);
    }

    private TableColumn<Integer[], Integer> getTableColumn(TableView<Integer[]> table, int colIndex) {
        final int currentCol = colIndex;
        TableColumn<Integer[], Integer> column = new TableColumn<>("Col " + (currentCol + 1));
        column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()[currentCol]));
        column.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        column.setOnEditCommit(event -> {
            Integer[] row = event.getRowValue();
            Integer newValue = event.getNewValue();
            if (newValue != null && (newValue == 0 || newValue == 1)) {
                row[currentCol] = newValue;
                G[event.getTablePosition().getRow()][currentCol] = newValue;
                updateMatrix();
            } else {
                showAlert("Invalid Input", "Please enter only 0 or 1.");
            }
            table.refresh();
        });
        return column;
    }

    @FXML
    private void toggleDebugMode() {
        debugMode = !debugMode;
        encoderDecoder.setDebug(debugMode);
        String status = debugMode ? "ON" : "OFF";
        System.out.println("Debug mode: " + status);
        showAlert("Debug Mode", "Debug mode is now " + status + ".");
    }

    private int[] getBlockFromInputBits() {
        int[] inputBits = getBitsFromInput();
        int end = Math.min(currentBitPosition + k, inputBits.length);
        int[] block = Arrays.copyOfRange(inputBits, currentBitPosition, end);
        currentBitPosition = end;
        if (block.length < k) {
            block = Arrays.copyOf(block, k);
        }
        return block;
    }

    private int[] getBitsFromInput() {
        String input = inputField.getText();
        return switch (inputTypeComboBox.getValue()) {
            case "Vector" -> parseInputToBits(inputField);
            case "Text" -> textProcessor.getBitRepresentation(input);
            case "Image" -> imageProcessor.getBitRepresentation(input);
            default -> new int[0];
        };
    }

    /**
     * Handles the "Encode" button action.
     * Encodes the input block and displays encoded and not encoded vectors.
     */
    @FXML
    public void encodeBlock() {
        try {
            notEncodedBits = getBlockFromInputBits();
            encodedBits = encoderDecoder.encode(notEncodedBits, G);

            blockLabel.setText("Block (m): " + Arrays.toString(notEncodedBits));
            encodedInputTextField.setText(Arrays.toString(encodedBits));
            notEncodedInputTextField.setText(Arrays.toString(notEncodedBits));
        } catch (Exception e) {
            showAlert("Error", "Failed to encode block. Please check your input.");
        }
    }

    /**
     * Handles the "Send" button action.
     * Simulates sending the encoded block through a noisy channel and displays the received and original blocks.
     */
    @FXML
    public void sendBlock() {
        try {
            receivedBitsForEncoded = encoderDecoder.introduceErrors(encodedBits, pe, q);
            receivedBitsForNotEncoded = encoderDecoder.introduceErrors(notEncodedBits, pe, q);

            errorLabel.setText("Errors: " + encoderDecoder.getIntroducedErrors());
            errorPositionLabel.setText("Error Positions: " + encoderDecoder.getErrorPositions().toString());
            receivedBitsEncodedTextField.setText(Arrays.toString(receivedBitsForEncoded));
            receivedNotEncodedTextField.setText(Arrays.toString(receivedBitsForNotEncoded));
        } catch (Exception e) {
            showAlert("Error", "Failed to send block. Please check your encoded data.");
        }
    }

    /**
     * Handles the "Decode" button action.
     * Decodes the received block, corrects errors, and displays the corrected and decoded blocks.
     */
    @FXML
    public void decodeBlock() {
        try {
            receivedBitsForEncoded = parseInputToBits(receivedBitsEncodedTextField);
            receivedBitsForNotEncoded = parseInputToBits(receivedNotEncodedTextField);

            int[] correctedEncoded = encoderDecoder.decode(receivedBitsForEncoded, H, encoderDecoder.findCosetLeaders(H));
            int[] correctedNotEncoded = encoderDecoder.decode(receivedBitsForNotEncoded, H, encoderDecoder.findCosetLeaders(H));

            int[] decodedEncodedBits = new int[k];
            System.arraycopy(correctedEncoded, 0, decodedEncodedBits, 0, k);

            int[] decodedNotEncodedBits = new int[k];
            System.arraycopy(correctedNotEncoded, 0, decodedNotEncodedBits, 0, k);

            correctedEncodedTextField.setText(Arrays.toString(correctedEncoded));
            correctedNotEncodedTextField.setText(Arrays.toString(correctedNotEncoded));
            decodedEncodedTextField.setText(Arrays.toString(decodedEncodedBits));
            decodedNotEncodedTextField.setText(Arrays.toString(decodedNotEncodedBits));
        } catch (Exception e) {
            showAlert("Error", "Failed to decode block. Please check your received data.");
        }
    }

    /**
     * Processes all steps (encode, send, decode) at once for all blocks.
     */
    @FXML
    public void processBlock() {
        if (debugMode) {
            encodeBlock();
            sendBlock();
            decodeBlock();
        } else {
            while (currentBitPosition < getBitsFromInput().length) {
                encodeBlock();
                sendBlock();
                decodeBlock();
            }
        }
    }

    private int[] parseInputToBits(TextField inputField) {
        String input = inputField.getText();
        return Arrays.stream(input.replaceAll("[\\[\\]]", "").split(",\\s*"))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setProbability() {
        try {
            pe = Double.parseDouble(errorProbabilityField.getText());
            probabilityLabel.setText("Error Probability (%): " + pe);
        } catch (NumberFormatException e) {
            showAlert("Invalid input", "Please enter a valid number for the probability of error.");
        }
    }

    public void setAlphabetSize() {
        try {
            q = Integer.parseInt(alphabetSizeField.getText());
            alphabetLabel.setText("Alphabet (q): " + q);
        } catch (NumberFormatException e) {
            showAlert("Invalid input", "Please enter a valid number for the alphabet size.");
        }
    }
}
