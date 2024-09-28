package processor;

public class TextProcessor extends Processor {
    public static int[] getBitRepresentation(String text) {
        StringBuilder bitStringBuilder = new StringBuilder();
        for (char c : text.toCharArray()) {
            String binaryString = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
            bitStringBuilder.append(binaryString);
        }
        String bitString = bitStringBuilder.toString();
        return bitString.chars().map(c -> c - '0').toArray();
    }
}
