package processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextProcessor extends Processor {
    public TextProcessor(EncoderDecoder encoderDecoder, int[][] G, int k, double pe, int q) {
        super(encoderDecoder, G, k, pe, q);
    }
    
    /** 
     * Processes a text by encoding it, introducing errors, and decoding it.
     * @param text text to process
     */
    public void processText(String text) {
        List<int[]> decodedResults = new ArrayList<>();

        // Convert text to bits
        StringBuilder bitStringBuilder = new StringBuilder();
        for (char c : text.toCharArray()) {
            String binaryString = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
            bitStringBuilder.append(binaryString);
        }
        String bitString = bitStringBuilder.toString();
        int[] bits = bitString.chars().map(c -> c - '0').toArray();
        sendChunk(decodedResults, bits);

        // Collect all decoded bits into a single array
        int[] allDecodedBits = decodedResults.stream().flatMapToInt(Arrays::stream).boxed().mapToInt(Integer::intValue).toArray();
        StringBuilder decodedText = getStringFromBits(allDecodedBits);

        System.out.println("\nIntroduced errors: " + encoderDecoder.getIntroducedErrors());
        System.out.printf("Decoded text: %s%n%n", decodedText);
    }

}
