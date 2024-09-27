package ui;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import model.CosetLeader;
import processor.EncoderDecoder;
import processor.ImageProcessor;
import processor.Processor;
import processor.TextProcessor;

import java.util.*;
import java.util.stream.IntStream;

@Getter
@Setter
@Accessors(chain = true)
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
    private int[] encodedBlock;
    private int[] blockWithError;
    private int[] blockWithoutCodeError;
    private int[] correctedBlock;
    private int[] decodedBlock;
    private int[] inputBits;
    private int currentBitPosition = 0;
    private Map<String, CosetLeader> cosetLeaders = new HashMap<>();
    private boolean debugMode = false;

    private Data() {
    }

    public static Data getInstance() {
        if (instance == null) {
            instance = new Data();
        }
        return instance;
    }

    public void generateGeneratingMatrix() {
        G = EncoderDecoder.generateGeneratingMatrix(k, n);
    }

    public void generateParityCheckMatrix() {
        H = EncoderDecoder.generateParityCheckMatrix(G);
    }

    public void generateCosetLeaders() {
        cosetLeaders = EncoderDecoder.findCosetLeaders(H, q);
    }

    public void generateInputBits(String input) {
        inputBits = switch (input) {
            case "Vector" -> Arrays.stream(input.split(","))
                    .map(String::trim)
                    .mapToInt(Integer::parseInt)
                    .toArray();
            case "Text" -> TextProcessor.getBitRepresentation(input);
            case "Image" -> ImageProcessor.getBitRepresentation(input);
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
        encodedBlock = EncoderDecoder.encode(block, G);
    }

    public void introduceErrors() {
        blockWithError = EncoderDecoder.introduceErrors(encodedBlock, pe, q);
        blockWithoutCodeError = EncoderDecoder.introduceErrors(block, pe, q);
        blocksWithoutCode.add(blockWithoutCodeError);
    }

    public void decodeBlock() {
        correctedBlock = EncoderDecoder.decodeStepByStep(blockWithError, H, cosetLeaders);
        decodedBlock = Arrays.copyOf(correctedBlock, k);
        System.out.println("Decoded codeword: " + Arrays.toString(decodedBlock));
        decodedBlocks.add(decodedBlock);
    }

    public int getErrorCount() {
        return (int) IntStream.range(0, blockWithError.length)
                .filter(i -> blockWithError[i] != encodedBlock[i])
                .count();
    }

    public int[] getErrorPositions() {
        return IntStream.range(0, blockWithError.length)
                .filter(i -> blockWithError[i] != encodedBlock[i])
                .toArray();
    }

    public int[] getNoCodingErrorPositions() {
        return IntStream.range(0, blockWithoutCodeError.length)
                .filter(i -> blockWithoutCodeError[i] != block[i])
                .toArray();
    }

    public int getNoCodingErrorCount() {
        return (int) IntStream.range(0, blockWithoutCodeError.length)
                .filter(i -> blockWithoutCodeError[i] != block[i])
                .count();
    }

    public StringBuilder getDecodedString() {
        return Processor.getStringFromBits(decodedBlocks.stream()
                .flatMapToInt(Arrays::stream)
                .toArray());
    }

    public StringBuilder getWithoutCodingString() {
        return Processor.getStringFromBits(blocksWithoutCode.stream()
                .flatMapToInt(Arrays::stream)
                .toArray());
    }
}
