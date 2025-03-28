package processor;

import lombok.Getter;
import lombok.Setter;
import model.CosetLeader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

@Getter
@Setter
public class Data {
    private static Data instance;

    private final List<int[]> decodedBlocks = new ArrayList<>();
    private final List<int[]> blocksWithoutCode = new ArrayList<>();
    private double pe = 0.0001;
    private int q = 2;
    private int[][] G;
    private int[][] H;
    private int n;
    private int k;
    private int[] block;
    private int[] blockWithoutCode;
    private int[] encodedBlock;
    private int[] blockWithError;
    private int[] blockWithoutCodeAndError;
    private int[] correctedBlock;
    private int[] decodedBlock;
    private int[] inputBits;
    private int currentBitPosition = 0;

    private int totalFixed = 0;
    private int totalErrors = 0;
    private int totalNoCodingErrors = 0;
    private int totalNoCodingFixed = 0;

    private int currentBlock = 0;
    private int totalBlocks = 0;

    private Map<String, CosetLeader> cosetLeaders = new HashMap<>();
    private boolean debugMode = true;

    private Processor processor = new Processor();
    private EncoderDecoder encoderDecoder = new EncoderDecoder();

    public static Data getInstance() {
        if (instance == null) {
            instance = new Data();
        }
        return instance;
    }

    public void generateGeneratingMatrix() {
        G = encoderDecoder.generateGeneratingMatrix(k, n);
    }

    public void generateParityCheckMatrix() {
        H = encoderDecoder.generateParityCheckMatrix(G);
    }

    public void generateCosetLeaders() {
        cosetLeaders = encoderDecoder.findCosetLeaders(H);
    }

    public void generateInputBits(String inputType, String input) {
        inputBits = switch (inputType) {
            case "Vector" -> processor.getBitRepresentationFromVector(input);
            case "Text" -> processor.getBitRepresentationFromText(input);
            case "Image" -> processor.getBitRepresentationFromImage(input);
            default -> new int[0];
        };
    }

    public void nextBlock() {
        if (currentBitPosition >= inputBits.length) {
            return;
        }
        block = Arrays.copyOfRange(inputBits, currentBitPosition, Math.min(currentBitPosition + k, inputBits.length));
        currentBitPosition += k;
        currentBlock++;
        totalBlocks = (int) Math.ceil((double) inputBits.length / k);
        System.out.printf("Block %d/%d: %s\n", currentBlock, totalBlocks, Arrays.toString(block));
        if (block.length < k) {
            block = Arrays.copyOf(block, k);
        }
    }

    public void encodeBlock() {
        encodedBlock = encoderDecoder.encode(block, G);
        blockWithoutCode = block;
    }

    public void introduceErrors() {
        blockWithError = encoderDecoder.introduceErrors(encodedBlock, pe, q);
        blockWithoutCodeAndError = encoderDecoder.introduceErrors(blockWithoutCode, pe, q);
        totalErrors += getErrorCount();
        totalNoCodingErrors += getNoCodingErrorCount();
    }

    public void decodeBlock() {
        correctedBlock = encoderDecoder.decodeStepByStep(blockWithError, H, cosetLeaders);
        decodedBlock = Arrays.copyOf(correctedBlock, k);
        blockWithoutCode = Arrays.copyOf(blockWithoutCode, k);
        totalFixed += getFixedCount();
        totalNoCodingFixed += getNoCodingFixedCount();
        if (Data.getInstance().isDebugMode()) {
            System.out.println("\nBlock: " + Arrays.toString(block));
            System.out.println("Encoded block: " + Arrays.toString(encodedBlock));
            System.out.println("Block with error: " + Arrays.toString(blockWithError));
            System.out.println("Corrected block: " + Arrays.toString(correctedBlock));
            System.out.println("Decoded block: " + Arrays.toString(decodedBlock));
            System.out.println("Block without code: " + Arrays.toString(blockWithoutCode));
            System.out.println("Block without code and error: " + Arrays.toString(blockWithoutCodeAndError));
        }
        decodedBlocks.add(decodedBlock);
        blocksWithoutCode.add(blockWithoutCodeAndError);
    }

    public int getErrorCount() {
        return getErrorCount(blockWithError, encodedBlock);
    }

    public int[] getErrorPositions() {
        return getErrorPositions(blockWithError, encodedBlock);
    }

    public int[] getNoCodingErrorPositions() {
        return getErrorPositions(blockWithoutCodeAndError, blockWithoutCode);
    }

    public int getNoCodingErrorCount() {
        return getErrorCount(blockWithoutCodeAndError, blockWithoutCode);
    }

    public StringBuilder getDecodedString() {
        return getStringFromBlocks(decodedBlocks);
    }

    public StringBuilder getWithoutCodingString() {
        return getStringFromBlocks(blocksWithoutCode);
    }

    public int getFixedCount() {
        return getFixedCount(decodedBlock, block, getErrorPositions());
    }

    public int[] getFixedPositions() {
        return getFixedPositions(decodedBlock, block, getErrorPositions());
    }

    public int getNoCodingFixedCount() {
        return getFixedCount(blockWithoutCodeAndError, block, getNoCodingErrorPositions());
    }

    public int[] getNoCodingFixedPositions() {
        return getFixedPositions(blockWithoutCodeAndError, block, getNoCodingErrorPositions());
    }

    private int getErrorCount(int[] array1, int[] array2) {
        return (int) IntStream.range(0, array1.length)
                .filter(i -> array1[i] != array2[i])
                .count();
    }

    private int[] getErrorPositions(int[] array1, int[] array2) {
        return IntStream.range(0, array1.length)
                .filter(i -> array1[i] != array2[i])
                .toArray();
    }

    private int getFixedCount(int[] array1, int[] array2, int[] errorPositions) {
        return (int) IntStream.range(0, array1.length)
                .filter(i -> array1[i] == array2[i] && Arrays.stream(errorPositions).anyMatch(j -> j == i))
                .count();
    }

    private int[] getFixedPositions(int[] array1, int[] array2, int[] errorPositions) {
        return IntStream.range(0, array1.length)
                .filter(i -> array1[i] == array2[i] && Arrays.stream(errorPositions).anyMatch(j -> j == i))
                .toArray();
    }

    private StringBuilder getStringFromBlocks(List<int[]> blocks) {
        return Processor.getStringFromBits(blocks.stream()
                .flatMapToInt(Arrays::stream)
                .toArray());
    }

    public void writeImage() {
        try {
            File dir = new File("img");
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
                }
            }

            int[] decodedImageArray = decodedBlocks.stream()
                    .flatMapToInt(Arrays::stream)
                    .toArray();
            String decodedImagePath = "img/img_decoded.png";
            processor.writeImage(decodedImageArray, decodedImagePath);
            verifyFileExists(decodedImagePath);

            int[] withoutCodeImageArray = blocksWithoutCode.stream()
                    .flatMapToInt(Arrays::stream)
                    .toArray();
            String withoutCodeImagePath = "img/img_without_code.png";
            processor.writeImage(withoutCodeImageArray, withoutCodeImagePath);
            verifyFileExists(withoutCodeImagePath);

        } catch (Exception e) {
            System.err.println("Failed to write images: " + e.getMessage());
        }
    }

    private void verifyFileExists(String path) throws IOException {
        if (!Files.exists(Paths.get(path))) {
            throw new IOException("File was not created: " + path);
        }
    }


    public void clear() {
        setTotalErrors(0);
        setTotalNoCodingErrors(0);
        setTotalBlocks(0);
        setCurrentBlock(0);
        setCurrentBitPosition(0);
        getDecodedBlocks().clear();
        getBlocksWithoutCode().clear();
    }
}
