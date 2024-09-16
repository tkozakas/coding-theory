package processor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageProcessor extends Processor {

    public ImageProcessor(EncoderDecoder encoderDecoder, int[][] G, int k, double pe, int q) {
        super(encoderDecoder, G, k, pe, q);
    }

    /**
     * Processes an image by reading it from the input path, encoding it, introducing errors, decoding it, and writing it to the output path.
     * @param inputPath path to the input image
     * @param outputPath path to write the output image
     */
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
            int[] allDecodedBits = decodedResults.stream().flatMapToInt(Arrays::stream).boxed().mapToInt(Integer::intValue).toArray();
            binaryToImage(allDecodedBits, width, height, outputPath);
            System.out.println("\nIntroduced errors: " + encoderDecoder.getIntroducedErrors());
            System.out.printf("Decoded image written to: %s%n%n", outputPath);
        } catch (IOException e) {
            System.out.println("Error processing image: " + e.getMessage());
        }
    }

    /**
     * Converts an array of bits to an image and writes it to the output path.
     * @param decodedBits array of bits
     * @param width width of the image
     * @param height height of the image
     * @param outputPath path to write the output image
     */
    private void binaryToImage(int[] decodedBits, int width, int height, String outputPath) throws IOException {
        BufferedImage decodedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int bitIndex = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitIndex + 24 > decodedBits.length) { // Ensure there are enough bits to read
                    break;
                }
                int red = 0, green = 0, blue = 0;
                for (int i = 0; i < 8; i++) {
                    red |= (decodedBits[bitIndex++] << (7 - i));
                }
                for (int i = 0; i < 8; i++) {
                    green |= (decodedBits[bitIndex++] << (7 - i));
                }
                for (int i = 0; i < 8; i++) {
                    blue |= (decodedBits[bitIndex++] << (7 - i));
                }
                int rgb = (red << 16) | (green << 8) | blue;
                decodedImage.setRGB(x, y, rgb);
            }
        }
        ImageIO.write(decodedImage, "png", new File(outputPath));
    }
}
