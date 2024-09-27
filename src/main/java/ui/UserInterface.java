package ui;

import processor.EncoderDecoder;
import processor.ImageProcessor;
import processor.Processor;
import processor.TextProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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
    private final List<int[]> decodedBlocks = new ArrayList<>();
    private int[] m;
    private int currentBitPosition = 0;
    private int[] bits;

    public UserInterface() {
        encoderDecoder = new EncoderDecoder();
        processor = new Processor(encoderDecoder, G, pe, q);
        textProcessor = new TextProcessor(encoderDecoder, G, k, pe, q);
        imageProcessor = new ImageProcessor(encoderDecoder, G, k, pe, q);
    }

    public void start() {
        mainMenu();
        inputMenu();
    }

    private void inputMenu() {
        if (G == null) {
            System.out.println("Error: Please enter the generating matrix first.");
            mainMenu();
        }
        while (true) {
            System.out.println("""
                    \nChoose an option:
                    1. Process Vector
                    2. Process Text
                    3. Process Image
                    4. Generate new matrix
                    5. Back to main menu
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
            System.out.printf("%n%nProbability of error: %.5f%n" +
                            "Number of symbols in the alphabet: %d%n" +
                            "Generator matrix: %s%n" +
                            "Input vector length: %d%n" +
                            "Input vector: %s%n%n",
                    pe, q,
                    G != null ? Arrays.deepToString(G) : "Empty",
                    k,
                    Arrays.toString(m));

            System.out.printf("""
                    \nChoose an option:
                    1. Input (vector, text, image) to process
                    2. Enter generating matrix
                    3. Generate generating matrix
                    4. Change probability of error
                    5. Change number of symbols in the alphabet
                    6. Debug mode (currently %s)
                    7. Run experiments
                    Choice:\s""", encoderDecoder.isDebug() ? "ON" : "OFF");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> inputMenu();
                case 2 -> enterMatrix();
                case 3 -> getMatrix();
                case 4 -> {
                    System.out.print("Enter the new probability of error: ");
                    pe = scanner.nextDouble();
                    mainMenu();
                }
                case 5 -> {
                    System.out.print("Enter the new number of symbols in the alphabet: ");
                    q = scanner.nextInt();
                    mainMenu();
                }
                case 6 -> {
                    encoderDecoder.setDebug(!encoderDecoder.isDebug());
                    mainMenu();
                }
                case 7 -> experiments();
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

        G = EncoderDecoder.generateGeneratingMatrix(k, n);
        mainMenu();
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
        mainMenu();
    }

    private void experiments() {
        // TODO Implement experiments
    }


    private void inputVector() {
        System.out.println("Enter the vector to encode:");
        m = scanner.nextLine().chars()
                .filter(Character::isDigit)
                .map(c -> c - '0')
                .toArray();

        if (m.length != k) {
            System.out.println("Error: The length of the vector should be equal to the number of rows (k) in the matrix.");
            inputVector();
        }

        processor = new Processor(encoderDecoder, G, pe, q);
        while (currentBitPosition < bits.length) {
            int[] block = getBlockFromInputBits();
            int[] decoded = processor.processBlock(block, k);
            System.out.println("Decoded block: " + Arrays.toString(decoded));
            decodedBlocks.add(decoded);
        }
        System.out.println("Decoded text: " + getDecodedText());
    }

    private void inputText() {
        System.out.println("Enter the text to encode:");
        String text = scanner.nextLine();

        bits = TextProcessor.getBitRepresentation(text);

        textProcessor = new TextProcessor(encoderDecoder, G, k, pe, q);
        while (currentBitPosition < bits.length) {
            int[] block = getBlockFromInputBits();
            int[] decoded = textProcessor.processBlock(block, k);
            System.out.println("Decoded block: " + Arrays.toString(decoded));
            decodedBlocks.add(decoded);
        }
        System.out.println("Decoded text: " + getDecodedText());
    }

    private void inputImage() {
        System.out.println("Enter the path to the image file:");
        String inputPath = scanner.nextLine();

        bits = ImageProcessor.getBitRepresentation(inputPath);

        imageProcessor = new ImageProcessor(encoderDecoder, G, k, pe, q);
        while (currentBitPosition < bits.length) {
            int[] block = getBlockFromInputBits();
            int[] decoded = imageProcessor.processBlock(block, k);
            System.out.println("Decoded block: " + Arrays.toString(decoded));
            decodedBlocks.add(decoded);
        }
        System.out.println("Decoded text: " + getDecodedText());
    }

    private int[] getBlockFromInputBits() {
        int end = Math.min(currentBitPosition + k, bits.length);
        int[] block = Arrays.copyOfRange(bits, currentBitPosition, end);
        currentBitPosition = end;
        if (block.length < k) {
            block = Arrays.copyOf(block, k);
        }
        return block;
    }

    private String getDecodedText() {
        StringBuilder decodedText = new StringBuilder();
        for (int[] block : decodedBlocks) {
            decodedText.append(Processor.getStringFromBits(block));
        }
        return decodedText.toString();
    }
}
