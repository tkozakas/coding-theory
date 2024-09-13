package processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TextProcessor extends Processor {
    private final int k;

    public TextProcessor(EncoderDecoder encoderDecoder, int[][] G, int k, int errorProbability) {
        super(encoderDecoder, G, errorProbability);
        this.k = k;
    }

    public void processText(String text) {
        List<int[]> decodedResults = new ArrayList<>();

        // Convert text to bits
        StringBuilder bitStringBuilder = new StringBuilder();
        for (char c : text.toCharArray()) {
            String binaryString = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
            bitStringBuilder.append(binaryString);
        }
        String bitString = bitStringBuilder.toString();

        // Process bits in blocks of size k
        for (int i = 0; i < bitString.length(); i += k) {
            String block = bitString.substring(i, Math.min(i + k, bitString.length()));
            // Initialize m with zeros
            int[] m = IntStream.range(0, k).map(_ -> 0).toArray();

            // Fill m with actual bits
            IntStream.range(0, block.length()).forEach(j -> m[j] = block.charAt(j) - '0');

            int[] decodedMessage = processBlock(m, k);
            decodedResults.add(decodedMessage);
        }

        // Collect all decoded bits into a single array
        int[] allDecodedBits = decodedResults.stream().flatMapToInt(Arrays::stream).boxed().mapToInt(Integer::intValue).toArray();
        StringBuilder decodedText = getStringFromBits(allDecodedBits);

        System.out.println("\nIntroduced errors: " + encoderDecoder.getIntroducedErrors());
        System.out.printf("Decoded text: %s%n%n", decodedText);
    }

}
