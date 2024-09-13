package processor;

import java.util.ArrayList;
import java.util.List;

public class TextProcessor extends Processor{
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
            int[] m = new int[k];
            for (int j = 0; j < block.length(); j++) {
                m[j] = block.charAt(j) - '0';
            }
            // Pad remaining bits with zeros if necessary
            int[] decodedMessage = processBlock(m, k);
            decodedResults.add(decodedMessage);
        }

        // Reconstruct text from decoded bits
        StringBuilder decodedBits = new StringBuilder();
        for (int[] decoded : decodedResults) {
            for (int bit : decoded) {
                decodedBits.append(bit);
            }
        }

        StringBuilder decodedText = new StringBuilder();
        for (int i = 0; i + 8 <= decodedBits.length(); i += 8) {
            String byteStr = decodedBits.substring(i, i + 8);
            int charCode = Integer.parseInt(byteStr, 2);
            decodedText.append((char) charCode);
        }

        System.out.println("Decoded text: " + decodedText);
    }
}
