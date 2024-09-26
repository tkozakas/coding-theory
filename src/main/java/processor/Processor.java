package processor;

import model.CosetLeader;

import java.util.Arrays;
import java.util.Map;

public class Processor {
    protected final processor.EncoderDecoder encoderDecoder;
    private final double pe;
    private final int[][] G;
    private final int q;

    public Processor(processor.EncoderDecoder encoderDecoder, int[][] G, double pe, int q) {
        this.encoderDecoder = encoderDecoder;
        this.pe = pe;
        this.q = q;
        this.G = G;
    }

    public static StringBuilder getStringFromBits(int[] bits) {
        StringBuilder bitsStringBuilder = new StringBuilder();
        for (int bit : bits) {
            bitsStringBuilder.append(bit);
        }
        String bitsString = bitsStringBuilder.toString();

        StringBuilder decodedText = new StringBuilder();
        for (int i = 0; i + 8 <= bitsString.length(); i += 8) {
            String byteStr = bitsString.substring(i, i + 8);
            int charCode = Integer.parseInt(byteStr, 2);
            decodedText.append((char) charCode);
        }
        return decodedText;
    }

    public int[] processBlock(int[] m, int k) {
        int[] c = encoderDecoder.encode(m, G);
        int[] r = encoderDecoder.introduceErrors(c, pe, q);
        int[][] H = encoderDecoder.generateParityCheckMatrix(G);
        Map<String, CosetLeader> cosetLeaders = encoderDecoder.findCosetLeaders(H, 2);
        int[] corrected = encoderDecoder.decodeStepByStep(r, H, cosetLeaders);
        int[] decoded = new int[k];
        System.arraycopy(corrected, 0, decoded, 0, k);
        System.out.println("\n=========================================\n");
        System.out.printf("Decoded message (m): %s (%s)%n", getStringFromBits(decoded), Arrays.toString(decoded));
        return decoded;
    }
}
