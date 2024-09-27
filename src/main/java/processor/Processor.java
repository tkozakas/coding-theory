package processor;

public class Processor {
    protected final processor.EncoderDecoder encoderDecoder;

    public Processor(processor.EncoderDecoder encoderDecoder) {
        this.encoderDecoder = encoderDecoder;
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

}
