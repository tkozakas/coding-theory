package processor;

import lombok.Getter;
import lombok.Setter;
import model.CosetLeader;

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
    private int[] blockWithoutCodeError;
    private int[] correctedBlock;
    private int[] decodedBlock;
    private int[] inputBits;
    private int currentBitPosition = 0;

    private Map<String, CosetLeader> cosetLeaders = new HashMap<>();
    private boolean debugMode = false;

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
        int end = Math.min(currentBitPosition + k, inputBits.length);
        block = Arrays.copyOfRange(inputBits, currentBitPosition, end);
        currentBitPosition = end;
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
        blockWithoutCodeError = encoderDecoder.introduceErrors(blockWithoutCode, pe, q);
    }

    public void decodeBlock() {
        correctedBlock = encoderDecoder.decodeStepByStep(blockWithError, H, cosetLeaders);
        decodedBlock = Arrays.copyOf(correctedBlock, k);
        System.out.println("Decoded codeword: " + Arrays.toString(decodedBlock));
        decodedBlocks.add(decodedBlock);
        blocksWithoutCode.add(blockWithoutCodeError);
    }

    public int getErrorCount() {
        return getErrorCount(blockWithError, encodedBlock);
    }

    public int[] getErrorPositions() {
        return getErrorPositions(blockWithError, encodedBlock);
    }

    public int[] getNoCodingErrorPositions() {
        return getErrorPositions(blockWithoutCodeError, blockWithoutCode);
    }

    public int getNoCodingErrorCount() {
        return getErrorCount(blockWithoutCodeError, blockWithoutCode);
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
        return getFixedCount(blockWithoutCodeError, block, getNoCodingErrorPositions());
    }

    public int[] getNoCodingFixedPositions() {
        return getFixedPositions(blockWithoutCodeError, block, getNoCodingErrorPositions());
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
        processor.writeImage(decodedBlocks.stream()
                .flatMapToInt(Arrays::stream)
                .toArray(), "img/img_decoded.png");
        processor.writeImage(blocksWithoutCode.stream()
                .flatMapToInt(Arrays::stream)
                .toArray(), "img/img_without_code.png");
    }
}
