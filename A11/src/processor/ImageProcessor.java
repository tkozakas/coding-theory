package processor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessor extends Processor {
    private final int k;

    public ImageProcessor(EncoderDecoder encoderDecoder, int[][] G, int k, double pe, int q) {
        super(encoderDecoder, G, pe, q);
        this.k = k;
    }

    public void processImage(String inputPath, String outputPath) {
        try {
            BufferedImage image = ImageIO.read(new File(inputPath));
            int width = image.getWidth();
            int height = image.getHeight();

            List<int[]> decodedResults = new ArrayList<>();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;

                    int[] bits = new int[24];
                    for (int i = 0; i < 8; i++) {
                        bits[i] = (red >> (7 - i)) & 1;
                        bits[i + 8] = (green >> (7 - i)) & 1;
                        bits[i + 16] = (blue >> (7 - i)) & 1;
                    }

                    sendChunk(decodedResults, bits);
                }
            }
            StringBuilder decodedBits = collectDecoded(decodedResults);
            binaryToImage(decodedBits.toString(), width, height, outputPath);
            System.out.println("\nIntroduced errors: " + encoderDecoder.getIntroducedErrors());
            System.out.printf("Decoded image written to: %s%n%n", outputPath);
        } catch (IOException e) {
            System.out.println("Error processing image: " + e.getMessage());
        }
    }

    private void sendChunk(List<int[]> decodedResults, int[] bits) {
        for (int i = 0; i < bits.length; i += k) {
            int[] m = new int[k];
            int bitsToCopy = Math.min(k, bits.length - i);
            System.arraycopy(bits, i, m, 0, bitsToCopy);
            // Pad with zeros if necessary

            int[] decodedMessage = processBlock(m, k);
            decodedResults.add(decodedMessage);
        }
    }

    private StringBuilder collectDecoded(List<int[]> decodedResults) {
        StringBuilder decodedBits = new StringBuilder();
        for (int[] decoded : decodedResults) {
            for (int bit : decoded) {
                decodedBits.append(bit);
            }
        }
        return decodedBits;
    }

    private void binaryToImage(String bitsString, int width, int height, String outputPath) throws IOException {
        BufferedImage decodedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int bitIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitIndex + 24 > bitsString.length()) {
                    break;
                }
                int red = 0, green = 0, blue = 0;
                for (int i = 0; i < 8; i++) {
                    red |= (bitsString.charAt(bitIndex++) - '0') << (7 - i);
                }
                for (int i = 0; i < 8; i++) {
                    green |= (bitsString.charAt(bitIndex++) - '0') << (7 - i);
                }
                for (int i = 0; i < 8; i++) {
                    blue |= (bitsString.charAt(bitIndex++) - '0') << (7 - i);
                }
                int rgb = (red << 16) | (green << 8) | blue;
                decodedImage.setRGB(x, y, rgb);
            }
        }
        ImageIO.write(decodedImage, "png", new File(outputPath));
    }
}
