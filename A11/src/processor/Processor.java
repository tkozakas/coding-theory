package processor;

import model.CosetLeader;

import java.util.List;

public class Processor {
    protected final EncoderDecoder encoderDecoder;
    private final double pe;
    private final int[][] G;
    private final int q;


    public Processor(EncoderDecoder encoderDecoder, int[][] G, double pe, int q) {
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
        List<CosetLeader> cosetLeaders = encoderDecoder.findCosetLeaders(H);
        int[] decoded = encoderDecoder.decode(r, H, cosetLeaders);

        // Extract the message from the decoded codeword
        int[] decodedMessage = new int[k];
        System.arraycopy(decoded, 0, decodedMessage, 0, k);
        return decodedMessage;
    }
}
