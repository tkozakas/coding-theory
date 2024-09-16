package ui;

import model.ExperimentResult;
import processor.EncoderDecoder;
import processor.ImageProcessor;
import processor.Processor;
import processor.TextProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

public class UserInterface {
    private final Scanner scanner = new Scanner(System.in);
    private final EncoderDecoder encoderDecoder;
    private Processor processor;
    private TextProcessor textProcessor;
    private ImageProcessor imageProcessor;

    private double pe = 0.0001; // Probability of error
    private int q = 2; // Number of symbols in the alphabet

    private int[][] G;
    private int n;
    private int k;

    public UserInterface() {
        encoderDecoder = new EncoderDecoder();
        processor = new Processor(encoderDecoder, G, k, pe, q);
        textProcessor = new TextProcessor(encoderDecoder, G, k, pe, q);
        imageProcessor = new ImageProcessor(encoderDecoder, G, k, pe, q);
    }

    public void start() {
        mainMenu();
        inputMenu();
    }

    private void inputMenu() {
        while (true) {
            System.out.println("""
                    \nChoose an option:
                    1. Process Vector
                    2. Process Text
                    3. Process Image
                    4. Generate new matrix
                    5. Exit
                    Choice:\s""");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> inputVector();
                case 2 -> inputText();
                case 3 -> inputImage();
                case 4 -> mainMenu();
                case 5 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void mainMenu() {
        while (true) {
            System.out.printf("%n%nProbability of error: %.5f%nNumber of symbols in the alphabet: %d%n%n", pe, q);
            System.out.printf("""
                    \nChoose an option:
                    1. Enter generating matrix
                    2. Generate generating matrix
                    3. Change probability of error
                    4. Change number of symbols in the alphabet
                    5. Debug mode (currently %s)
                    6. Run experiments
                    Choice:\s""", encoderDecoder.isDebug() ? "ON" : "OFF");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> enterMatrix();
                case 2 -> getMatrix();
                case 3 -> {
                    System.out.print("Enter the new probability of error: ");
                    pe = scanner.nextDouble();
                    mainMenu();
                }
                case 4 -> {
                    System.out.print("Enter the new number of symbols in the alphabet: ");
                    q = scanner.nextInt();
                    mainMenu();
                }
                case 5 -> {
                    encoderDecoder.setDebug(!encoderDecoder.isDebug());
                    mainMenu();
                }
                case 6 -> experiments();
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void getMatrix() {
        System.out.print("Enter the number of columns (n): ");
        n = scanner.nextInt();
        System.out.print("Enter the number of rows (k): ");
        k = scanner.nextInt();
        scanner.nextLine();

        G = generateMatrix(k, n);
        inputMenu();
    }

    private int[][] generateMatrix(int k, int n) {
        int[][] G = new int[k][n];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                G[i][j] = i == j ? 1 : 0;
            }
            for (int j = k; j < n; j++) {
                G[i][j] = (int) (Math.random() * 2);
            }
        }
        return G;
    }

    private void enterMatrix() {
        try {
            System.out.print("Enter the number of columns (n): ");
            n = scanner.nextInt();
            System.out.print("Enter the number of rows (k): ");
            k = scanner.nextInt();
            scanner.nextLine();

            G = new int[k][n];
            if (k > n) {
                System.out.println("Error: The number of rows (k) should be less than or equal to the number of columns (n).");
                enterMatrix();
            } else {
                System.out.println("Enter the matrix row by row (only 0s and 1s):");
                for (int i = 0; i < k; i++) {
                    String rowInput = scanner.nextLine();
                    String[] numbers = rowInput.trim().split("\\s+");
                    if (numbers.length != n) {
                        System.out.println("Error: Incorrect number of columns. Please enter the row again.");
                        i--;
                        continue;
                    }
                    for (int j = 0; j < n; j++) {
                        G[i][j] = Integer.parseInt(numbers[j]);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: Invalid input. Please try again.");
            enterMatrix();
        }
        inputMenu();
    }

    private void experiments() {
        System.out.println("From 0 to x: ");
        int x = scanner.nextInt();
        System.out.println("Running experiments...");
        List<ExperimentResult> results = new ArrayList<>();

        for (int n = 8; n <= x; n *= 2) {
            for (int k = 8; k <= x; k *= 2) {
                if (n <= k) {
                    continue;
                }
                for (double pe = 0.0001; pe <= 0.9; pe *= 2) {
                    long startTime = System.nanoTime();

                    int[][] G = generateMatrix(k, n);

                    // Run a test encoding/decoding
                    int[] testVector = IntStream.range(0, k).map(_ -> (int) (Math.random() * 2)).toArray();
                    Processor processor = new Processor(encoderDecoder, G, k, pe, q);
                    int[] decoded = processor.processBlock(testVector, k);

                    long endTime = System.nanoTime();

                    if (decoded == null || decoded.length == 0) {
                        continue;
                    }

                    double duration = (endTime - startTime) / 1e6; // Convert to milliseconds
                    results.add(new ExperimentResult(n, k, pe, duration));
                }
            }
        }

        System.out.println("Experiments completed.");
        System.out.println("Results:");
        System.out.println("n \t\t\t k \t\t\t pe \t\t Duration (ms)");
        for (ExperimentResult result : results) {
            System.out.printf("%d \t\t\t %d \t\t\t %.5f \t\t %.2f%n", result.n(), result.k(), result.pe(), result.duration());
        }
    }


    private void inputVector() {
        System.out.println("Enter the vector to encode:");
        int[] m = scanner.nextLine().chars()
                .filter(Character::isDigit)
                .map(c -> c - '0')
                .toArray();

        if (m.length != k) {
            System.out.println("Error: The length of the vector should be equal to the number of rows (k) in the matrix.");
            inputVector();
        }

        processor = new Processor(encoderDecoder, G, k, pe, q);
        processor.processBlock(m, k);
    }

    private void inputText() {
        System.out.println("Enter the text to encode:");
        String text = scanner.nextLine();

        textProcessor = new TextProcessor(encoderDecoder, G, k, pe, q);
        textProcessor.processText(text);
    }

    private void inputImage() {
        System.out.println("Enter the path to the image file:");
        String inputPath = scanner.nextLine();
        System.out.println("Enter the output path for the decoded image:");
        String outputPath = scanner.nextLine();

        imageProcessor = new ImageProcessor(encoderDecoder, G, k, pe, q);
        imageProcessor.processImage(inputPath, outputPath);
    }
}
