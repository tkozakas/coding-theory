package processor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessor extends Processor {

    public ImageProcessor(EncoderDecoder encoderDecoder, int[][] G, int k, double pe, int q) {
        super(encoderDecoder, G, pe, q);
    }

    public int[] getBitRepresentation(String inputPath) {
        try {
            BufferedImage image = ImageIO.read(new File(inputPath));
            int width = image.getWidth();
            int height = image.getHeight();

            List<Integer> bitsList = new ArrayList<>();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;

                    for (int i = 0; i < 8; i++) {
                        bitsList.add((red >> (7 - i)) & 1);
                        bitsList.add((green >> (7 - i)) & 1);
                        bitsList.add((blue >> (7 - i)) & 1);
                    }
                }
            }
            return bitsList.stream().mapToInt(Integer::intValue).toArray();
        } catch (IOException e) {
            System.out.println("Error reading image: " + e.getMessage());
            return new int[0];
        }
    }
}
