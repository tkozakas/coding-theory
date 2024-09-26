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
import processor.Processor;
import processor.TextProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FxUserInterface {
    public TableView<Integer[]> generatingMatrixTable;
    public TableView<Integer[]> parityCheckMatrixTable;
    public TableView<CosetLeader> cosetLeaderTable;

    public ComboBox<String> inputTypeComboBox;

    public TextField columnsField;
    public TextField rowsField;
    public TextField totalCosetLeaders;
    public TextField inputTextField;
    public TextField errorProbabilityField;
    public TextField alphabetSizeField;

    public TextField encodedBlockTextField;
    public TextField receivedBlockTextField;
    public TextField correctedBlockTextField;
    public TextField decodedBlockTextField;

    public Label errorCountLabel;
    public Label errorPositionsLabel;
    public Label currentBlockLabel;
    public Label probabilityLabel;
    public Label alphabetLabel;
    public Label decodedTextLabel;
    public Label inputTextLabel;
    public Label errorsFixedLabel;
    public Label letterLabel;
    public Label introducedErrorCountLabel;
    public Label totalFixedErrorsLabel;

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

    private int[] block;
    private int[] inputBits;
    private int currentBitPosition = 0;

    private final List<int[]> decodedBlocks = new ArrayList<>();
    private Map<String, CosetLeader> cosetLeaders;

    @FXML
    private void initialize() {
        inputTypeComboBox.getSelectionModel().selectFirst();
        encoderDecoder = new EncoderDecoder();
        alphabetLabel.setText("Alphabet (q): " + q);
        probabilityLabel.setText("Error Probability (p): " + pe);
    }

    private void updateMatrix() {
        H = encoderDecoder.generateParityCheckMatrix(G);
        setupMatrixTable(parityCheckMatrixTable, H, n - k, n);
        cosetLeaders = encoderDecoder.findCosetLeaders(H, 2);
        setupCosetLeaderTable(cosetLeaders);
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
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(inputTextField.getScene().getWindow());
        if (selectedFile != null) {
            inputTextField.setText(selectedFile.getPath());
        }
        decodedTextLabel.setText("Decoded Image:");
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
            cosetLeaders = encoderDecoder.findCosetLeaders(H, 2);
            setupCosetLeaderTable(cosetLeaders);

            textProcessor = new TextProcessor(encoderDecoder, G, k, pe, q);
            imageProcessor = new ImageProcessor(encoderDecoder, G, k, pe, q);

        } catch (NumberFormatException e) {
            showAlert("Invalid input", "Please enter valid integers for n and k.");
        }
    }

    private void setupCosetLeaderTable(Map<String, CosetLeader> cosetLeaders) {
        cosetLeaderTable.getColumns().clear();
        cosetLeaderTable.getItems().clear();

        TableColumn<CosetLeader, String> syndromeColumn = new TableColumn<>("Syndrome");
        syndromeColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(Arrays.toString(cellData.getValue().syndrome())));
        cosetLeaderTable.getColumns().add(syndromeColumn);

        TableColumn<CosetLeader, String> errorPatternColumn = new TableColumn<>("Error Pattern");
        errorPatternColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(Arrays.toString(cellData.getValue().errorPattern())));
        cosetLeaderTable.getColumns().add(errorPatternColumn);

        TableColumn<CosetLeader, String> weightColumn = new TableColumn<>("Weight");
        weightColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().weight()).asString());
        cosetLeaderTable.getColumns().add(weightColumn);

        cosetLeaderTable.getItems().addAll(cosetLeaders.values());
        cosetLeaderTable.getSortOrder().add(weightColumn);
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
        int end = Math.min(currentBitPosition + k, inputBits.length);
        int[] block = Arrays.copyOfRange(inputBits, currentBitPosition, end);
        currentBitPosition = end;
        if (block.length < k) {
            block = Arrays.copyOf(block, k);
        }
        return block;
    }

    private int[] getBitsFromInput() {
        String input = inputTextField.getText();
        return switch (inputTypeComboBox.getValue()) {
            case "Vector" -> parseInputToBits(inputTextField.getText());
            case "Text" -> textProcessor.getBitRepresentation(input);
            case "Image" -> imageProcessor.getBitRepresentation(input);
            default -> new int[0];
        };
    }

    @FXML
    public void encodeBlock() {
        try {
            clearResults();
            block = getBlockFromInputBits();
            letterLabel.setText("Letter: " + Processor.getStringFromBits(block));
            currentBlockLabel.setText("Current Block (m): " + Arrays.toString(block));
            block = encoderDecoder.encode(block, G);
            encodedBlockTextField.setText(Arrays.toString(block));
        } catch (Exception e) {
            showAlert("Error", "Failed to encode block. Please check your input.");
        }
    }

    @FXML
    public void sendBlock() {
        try {
            int currentErrorsCount = encoderDecoder.getIntroducedErrors();
            block = encodedBlockTextField.getText().isEmpty() ? block : parseInputToBits(encodedBlockTextField.getText());
            block = encoderDecoder.introduceErrors(block, pe, q);

            introducedErrorCountLabel.setText("Introduced Errors: " + (encoderDecoder.getIntroducedErrors() - currentErrorsCount));
            errorCountLabel.setText("Errors: " + encoderDecoder.getIntroducedErrors());
            errorPositionsLabel.setText("Error Positions: " + encoderDecoder.getErrorPositions().toString());
            receivedBlockTextField.setText(Arrays.toString(block));
        } catch (Exception e) {
            showAlert("Error", "Failed to send block. Please check your encoded data.");
        }
    }

    @FXML
    public void decodeBlock() {
        try {
            int currentFixedErrors = encoderDecoder.getFixedErrors();
            block = receivedBlockTextField.getText().isEmpty() ? block : parseInputToBits(receivedBlockTextField.getText());

            block = encoderDecoder.decodeStepByStep(block, H, cosetLeaders);
            correctedBlockTextField.setText(Arrays.toString(block));

            int[] decodedEncodedBits = new int[k];
            System.arraycopy(block, 0, decodedEncodedBits, 0, k);

            decodedBlocks.add(decodedEncodedBits);
            decodedBlockTextField.setText(Arrays.toString(decodedEncodedBits));
            if (inputTypeComboBox.getValue().equals("Image")) {
                decodedTextLabel.setText("Decoded Image (img/img_decoded.png)");
                imageProcessor.writeImage(decodedEncodedBits);
            } else {
                decodedTextLabel.setText("Decoded Text: " + getDecodedText());
            }
            errorsFixedLabel.setText("Errors Fixed: " + (encoderDecoder.getFixedErrors() - currentFixedErrors));
            totalFixedErrorsLabel.setText("Total Fixed Errors: " + encoderDecoder.getFixedErrors());
        } catch (Exception e) {
            showAlert("Error", "Failed to decode block. Please check your received data.");
        }
    }

    private String getDecodedText() {
        List<Integer> allBits = decodedBlocks.stream()
                .flatMapToInt(Arrays::stream).boxed()
                .toList();

        int[] allBitsArray = allBits.stream().mapToInt(Integer::intValue).toArray();
        return Processor.getStringFromBits(allBitsArray).toString();
    }

    @FXML
    public void processBlock() {
        inputBits = getBitsFromInput();
        if (debugMode) {
            encodeBlock();
            sendBlock();
            decodeBlock();
        } else {
            while (currentBitPosition < inputBits.length) {
                encodeBlock();
                sendBlock();
                decodeBlock();
            }
        }
    }

    private int[] parseInputToBits(String input) {
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

    private void clearResults() {
        encodedBlockTextField.clear();
        receivedBlockTextField.clear();
        correctedBlockTextField.clear();
        decodedBlockTextField.clear();
        encoderDecoder.clearErrors();
        encoderDecoder.clearErrorPositions();

        letterLabel.setText("Letter: -");
        currentBlockLabel.setText("Current Block (m):");
        errorPositionsLabel.setText("Error Positions: []");
        introducedErrorCountLabel.setText("Introduced Errors: 0");
    }

    public void setInput() {
        clearResults();
        decodedBlocks.clear();
        currentBitPosition = 0;
        decodedTextLabel.setText("Decoded Text:");
        totalFixedErrorsLabel.setText("Total Fixed Errors: 0");
        errorCountLabel.setText("Errors Total: 0");
        errorsFixedLabel.setText("Errors Fixed: 0");

        if (G == null) {
            showAlert("Error", "Please generate a matrix first.");
            return;
        }
        inputBits = getBitsFromInput();
        inputTextLabel.setText("Input Text: " + inputTextField.getText());
    }
}
