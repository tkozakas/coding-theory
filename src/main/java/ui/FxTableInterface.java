package ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import model.CosetLeader;

import java.util.Arrays;

public class FxTableInterface {

    private final Data data;
    public TextField columnsField;
    public TextField rowsField;
    public TableView<Integer[]> generatingMatrixTable;
    public TableView<Integer[]> parityCheckMatrixTable;
    public TableView<CosetLeader> cosetLeaderTable;
    public TextField totalCosetLeaders;

    public FxTableInterface() {
        this.data = Data.getInstance();
    }

    public void generateMatrix() {
        data.setN(Integer.parseInt(columnsField.getText()));
        data.setK(Integer.parseInt(rowsField.getText()));

        if (data.getN() <= data.getK()) {
            showAlert("The number of rows (k) should be less or equal to the number of columns (n).");
            return;
        }

        data.generateGeneratingMatrix();
        setupMatrixTable(generatingMatrixTable, data.getG(), data.getK(), data.getN());
        data.generateParityCheckMatrix();
        setupMatrixTable(parityCheckMatrixTable, data.getH(), data.getN() - data.getK(), data.getN());
        data.generateCosetLeaders();
        setupCosetLeaderTable();
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
                data.getG()[event.getTablePosition().getRow()][currentCol] = newValue;
                data.setG(data.getG());
                updateMatrix();
            } else {
                showAlert("Please enter only 0 or 1.");
            }
            table.refresh();
        });
        return column;
    }

    private void updateMatrix() {
        data.generateParityCheckMatrix();
        setupMatrixTable(parityCheckMatrixTable, data.getH(), data.getN() - data.getK(), data.getN());
        data.generateCosetLeaders();
        setupCosetLeaderTable();
    }

    private void setupCosetLeaderTable() {
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

        cosetLeaderTable.getItems().addAll(data.getCosetLeaders().values());
        cosetLeaderTable.getSortOrder().add(weightColumn);
        totalCosetLeaders.setText(String.valueOf(data.getCosetLeaders().size()));
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
