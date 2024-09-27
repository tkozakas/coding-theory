package ui;

import processor.EncoderDecoder;

import java.util.Arrays;
import java.util.Scanner;

public class UserInterface {
    private final Scanner scanner = new Scanner(System.in);
    private final Data data = Data.getInstance();
    private String inputType;

    public void start() {
        mainMenu();
    }

    private void mainMenu() {
        while (true) {
            displayMainMenu();
            int choice = getUserChoice();

            switch (choice) {
                case 1 -> inputMenu();
                case 2 -> enterMatrix();
                case 3 -> generateMatrix();
                case 4 -> changeProbability();
                case 5 -> changeAlphabetSize();
                case 6 -> toggleDebugMode();
                case 7 -> experiments();
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void displayMainMenu() {
        System.out.printf("%n%nProbability of error: %.5f%n" +
                        "Number of symbols in the alphabet: %d%n" +
                        "Generator matrix: %s%n" +
                        "Input vector length: %d%n" +
                        "Input vector: %s%n%n",
                data.getPe(), data.getQ(),
                data.getG() != null ? "\n" + EncoderDecoder.printMatrix(data.getG()) : "Empty",
                data.getK(),
                data.getBlock() != null ? Arrays.toString(data.getBlock()) : "Empty");

        System.out.printf("""
                \nChoose an option:
                1. Input (vector, text, image) to process
                2. Enter generating matrix
                3. Generate generating matrix
                4. Change probability of error
                5. Change number of symbols in the alphabet
                6. Debug mode (currently %s)
                7. Run experiments
                Choice:\s""", data.isDebugMode() ? "ON" : "OFF");
    }

    private void inputMenu() {
        if (data.getG() == null) {
            System.out.println("Error: Please enter the generating matrix first.");
            mainMenu();
        }

        while (true) {
            displayInputMenu();
            int choice = getUserChoice();

            switch (choice) {
                case 1 -> processVector();
                case 2 -> processText();
                case 3 -> processImage();
                case 4 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void displayInputMenu() {
        System.out.println("""
                \nChoose an option:
                1. Process Vector
                2. Process Text
                3. Process Image
                4. Back to main menu
                Choice:\s""");
    }

    private void generateMatrix() {
        System.out.print("Enter the number of columns (n): ");
        data.setN(scanner.nextInt());
        System.out.print("Enter the number of rows (k): ");
        data.setK(scanner.nextInt());
        scanner.nextLine();
        data.generateGeneratingMatrix();
        data.generateParityCheckMatrix();
        data.generateCosetLeaders();
    }

    private void enterMatrix() {
        try {
            System.out.print("Enter the number of columns (n): ");
            data.setN(scanner.nextInt());
            System.out.print("Enter the number of rows (k): ");
            data.setK(scanner.nextInt());
            scanner.nextLine();

            int[][] matrix = new int[data.getK()][data.getN()];
            if (data.getK() > data.getN()) {
                System.out.println("Error: The number of rows (k) should be less than or equal to the number of columns (n).");
                return;
            }

            System.out.println("Enter the matrix row by row (only 0s and 1s):");
            for (int i = 0; i < data.getK(); i++) {
                String[] numbers = scanner.nextLine().trim().split("\\s+");
                if (numbers.length != data.getN()) {
                    System.out.println("Error: Incorrect number of columns. Please enter the row again.");
                    i--;
                    continue;
                }
                for (int j = 0; j < data.getN(); j++) {
                    matrix[i][j] = Integer.parseInt(numbers[j]);
                }
            }
            data.setG(matrix);
            data.generateParityCheckMatrix();
            data.generateCosetLeaders();
        } catch (Exception e) {
            System.out.println("Error: Invalid input. Please try again.");
            scanner.nextLine();
        }
    }

    private void changeProbability() {
        System.out.print("Enter the new probability of error: ");
        data.setPe(scanner.nextDouble());
        scanner.nextLine();
    }

    private void changeAlphabetSize() {
        System.out.print("Enter the new number of symbols in the alphabet: ");
        data.setQ(scanner.nextInt());
        scanner.nextLine();
    }

    private void toggleDebugMode() {
        data.setDebugMode(!data.isDebugMode());
    }

    private void experiments() {
        // TODO Implement experiments
    }

    private void processVector() {
        inputType = "Vector";
        System.out.println("Enter the vector to encode:");
        String vector = scanner.nextLine();
        data.generateInputBits(inputType, vector);
        processInputBlocks();
    }

    private void processText() {
        inputType = "Text";
        System.out.println("Enter the text to encode:");
        String text = scanner.nextLine();
        data.generateInputBits(inputType, text);
        processInputBlocks();
    }

    private void processImage() {
        inputType = "Image";
        System.out.println("Enter the path to the image file:");
        String imagePath = scanner.nextLine();
        data.generateInputBits(inputType, imagePath);
        processInputBlocks();
    }

    private void processInputBlocks() {
        while (data.getCurrentBitPosition() < data.getInputBits().length) {
            data.nextBlock();
            data.encodeBlock();
            data.introduceErrors();
            data.decodeBlock();
        }
        System.out.println(inputType.equals("Image") ?
                "Decoded image saved as img/img_decoded.png" :
                "Decoded text: " + data.getDecodedString());
    }

    private int getUserChoice() {
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a number: ");
            scanner.next();
        }
        int choice = scanner.nextInt();
        scanner.nextLine();
        return choice;
    }
}
