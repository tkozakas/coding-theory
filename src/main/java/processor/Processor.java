package processor;

import model.CosetLeader;

import java.util.List;

public class Processor {
    protected final processor.EncoderDecoder encoderDecoder;
    private final double pe;
    private final int[][] G;
    private final int q;

    private int[] r;
    private int[] c;
    private int[] corrected;
    private int[] decoded;

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
        c = encoderDecoder.encode(m, G);
        r = encoderDecoder.introduceErrors(c, pe, q);
        int[][] H = encoderDecoder.generateParityCheckMatrix(G);
        List<CosetLeader> cosetLeaders = encoderDecoder.findCosetLeaders(H);
        corrected = encoderDecoder.decode(r, H, cosetLeaders);
        decoded = new int[k];
        System.arraycopy(corrected, 0, decoded, 0, k);
        return decoded;
    }

    public int[] getR() {
        return r;
    }

    public int[] getC() {
        return c;
    }

    public int[] getCorrected() {
        return corrected;
    }

    public int[] getDecoded() {
        return decoded;
    }
}
