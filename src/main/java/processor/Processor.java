package processor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Processor {

    public int[] getBitRepresentationFromImage(String inputPath) {
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

    public int[] getBitRepresentationFromVector(String input) {
        return Arrays.stream(input.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
    }

    public int[] getBitRepresentationFromText(String text) {
        StringBuilder bitStringBuilder = new StringBuilder();
        for (char c : text.toCharArray()) {
            String binaryString = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
            bitStringBuilder.append(binaryString);
        }
        String bitString = bitStringBuilder.toString();
        return bitString.chars().map(c -> c - '0').toArray();
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

    public void writeImage(int[] decodedEncodedBits, String outputPath) {
        try {
            int width = 256;
            int height = 256;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            List<Integer> rgbList = getRgbList(decodedEncodedBits);

            int index = 0;
            for (int y = 0; y < height && index + 2 < rgbList.size(); y++) {
                for (int x = 0; x < width && index + 2 < rgbList.size(); x++) {
                    int red = rgbList.get(index++);
                    int green = rgbList.get(index++);
                    int blue = rgbList.get(index++);
                    int rgb = (red << 16) | (green << 8) | blue;
                    image.setRGB(x, y, rgb);
                }
            }
            ImageIO.write(image, "png", new File(outputPath));
        } catch (IOException e) {
            System.out.println("Error writing image: " + e.getMessage());
        }
    }

    private List<Integer> getRgbList(int[] decodedEncodedBits) {
        List<Integer> rgbList = new ArrayList<>();
        for (int i = 0; i + 24 <= decodedEncodedBits.length; i += 24) {
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
