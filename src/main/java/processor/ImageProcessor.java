package processor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessor extends Processor {

    public ImageProcessor(EncoderDecoder encoderDecoder, int[][] G, double pe, int q) {
        super(encoderDecoder);
    }

    public static int[] getBitRepresentation(String inputPath) {
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

    public static void writeImage(int[] decodedEncodedBits) {
        try {
            int width = 256;
            int height = 256;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            List<Integer> rgbList = getRgbList(decodedEncodedBits);

            int index = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int red = rgbList.get(index++);
                    int green = rgbList.get(index++);
                    int blue = rgbList.get(index++);
                    int rgb = (red << 16) | (green << 8) | blue;
                    image.setRGB(x, y, rgb);
                }
            }
            ImageIO.write(image, "png", new File("img/img_decoded.png"));
        } catch (IOException e) {
            System.out.println("Error writing image: " + e.getMessage());
        }
    }

    private static List<Integer> getRgbList(int[] decodedEncodedBits) {
        List<Integer> rgbList = new ArrayList<>();
        for (int i = 0; i < decodedEncodedBits.length; i += 24) {
            int red = 0;
            int green = 0;
            int blue = 0;
            for (int j = 0; j < 8; j++) {
                red = (red << 1) | decodedEncodedBits[i + j];
                green = (green << 1) | decodedEncodedBits[i + 8 + j];
                blue = (blue << 1) | decodedEncodedBits[i + 16 + j];
            }
            rgbList.add(red);
            rgbList.add(green);
            rgbList.add(blue);
        }
        return rgbList;
    }
}
