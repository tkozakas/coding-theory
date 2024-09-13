package processor;

import model.CosetLeader;

import java.util.List;

public abstract class Processor {
    private final int errorProbability;
    private final EncoderDecoder encoderDecoder;
    private final int[][] G;

    public Processor(EncoderDecoder encoderDecoder, int[][] G, int errorProbability) {
        this.encoderDecoder = encoderDecoder;
        this.G = G;
        this.errorProbability = errorProbability;
    }

    protected int[] processBlock(int[] m, int k) {
        int[] c = encoderDecoder.encode(m, G);
        int[] r = encoderDecoder.introduceErrors(c, errorProbability);
        int[][] H = encoderDecoder.generateParityCheckMatrix(G);
        List<CosetLeader> cosetLeaders = encoderDecoder.findCosetLeaders(H);
        int[] decoded = encoderDecoder.decode(r, H, cosetLeaders);

        // Extract the message from the decoded codeword
        int[] decodedMessage = new int[k];
        System.arraycopy(decoded, 0, decodedMessage, 0, k);
        return decodedMessage;
    }
}
