package ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import processor.EncoderDecoder;
import processor.ImageProcessor;
import processor.Processor;
import processor.TextProcessor;

public class FxUserInterface {

    @FXML
    private ComboBox<String> inputTypeComboBox;

    @FXML
    private TextField inputField, columnsField, rowsField;

    @FXML
    private TableView<Integer[]> generatingMatrixTable, parityCheckMatrixTable;

    private boolean debugMode = false;
    private EncoderDecoder encoderDecoder;

    private final double pe = 0.0001; // Probability of error
    private final int q = 2; // Number of symbols in the alphabet

    private int[][] G;
    private int n;
    private int k;

    @FXML
    private void initialize() {
        inputTypeComboBox.getSelectionModel().selectFirst();
        encoderDecoder = new EncoderDecoder();
    }

    @FXML
    private void handleInputTypeSelection() {
        String selectedType = inputTypeComboBox.getValue();
        inputField.setPromptText(switch (selectedType) {
            case "Vector" -> "Enter vector (e.g., 1 0 1)";
            case "Text" -> "Enter text";
            case "Image" -> "Enter image path";
            default -> "Enter input";
        });
        System.out.println("Selected input type: " + selectedType);
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

            G = generateGeneratingMatrix(k, n);
            setupMatrixTable(generatingMatrixTable, G, k, n);
            int[][] H = generateParityCheckMatrix(G);
            setupMatrixTable(parityCheckMatrixTable, H, n - k, n);

        } catch (NumberFormatException e) {
            showAlert("Invalid input", "Please enter valid integers for n and k.");
        }
    }

    private int[][] generateGeneratingMatrix(int k, int n) {
        int[][] matrix = new int[k][n];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                matrix[i][j] = (i == j) ? 1 : 0;
            }
            for (int j = k; j < n; j++) {
                matrix[i][j] = (int) (Math.random() * 2);
            }
        }
        return matrix;
    }

    private int[][] generateParityCheckMatrix(int[][] G) {
        return encoderDecoder.generateParityCheckMatrix(G);
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

    @FXML
    private void processBlock() {
        try {
            String selectedType = inputTypeComboBox.getValue();
            switch (selectedType) {
                case "Vector" -> inputVector();
                case "Text" -> inputText();
                case "Image" -> inputImage();
                default -> showAlert("Error", "Invalid input type selected.");
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to process block. Please check your input and try again.");
        }
    }

    private void inputVector() {
        try {
            int[] m = inputField.getText().chars()
                    .filter(Character::isDigit)
                    .map(c -> c - '0')
                    .toArray();

            // TODO: fill with zeros if the length of the vector is less than k (same as in TextProcessor)
            if (m.length != k) {
                showAlert("Error", "The length of the vector should be equal to the number of rows (k) in the matrix.");
                return;
            }

            Processor processor = new Processor(encoderDecoder, G, k, pe, q);
            processor.processBlock(m, k);
        } catch (Exception e) {
            showAlert("Error", "Invalid vector input. Please enter a valid binary vector.");
        }
    }

    private void inputText() {
        try {
            String text = inputField.getText();
            TextProcessor textProcessor = new TextProcessor(encoderDecoder, G, k, pe, q);
            textProcessor.processText(text);
        } catch (Exception e) {
            showAlert("Error", "Failed to process text. Please check your input and try again.");
        }
    }

    private void inputImage() {
        try {
            String inputPath = inputField.getText();
            String outputPath = "decoded_image_output.png";

            ImageProcessor imageProcessor = new ImageProcessor(encoderDecoder, G, k, pe, q);
            imageProcessor.processImage(inputPath, outputPath);
        } catch (Exception e) {
            showAlert("Error", "Failed to process image. Please check the path and try again.");
        }
    }

    @FXML
    private void process() {
        try {
            processBlock();
        } catch (Exception e) {
            showAlert("Error", "Failed to process the whole input. Please try again.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
