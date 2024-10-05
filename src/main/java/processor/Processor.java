package processor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Processor {
    private BufferedImage inputImage;

    public int[] getBitRepresentationFromImage(String inputPath) {
        try {
            inputImage = ImageIO.read(new File(inputPath));
            int width = inputImage.getWidth();
            int height = inputImage.getHeight();
            int[] bitsArray = new int[width * height * 24];
            int bitIndex = 0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = inputImage.getRGB(x, y);
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;

                    for (int i = 7; i >= 0; i--) {
                        bitsArray[bitIndex++] = (red >> i) & 1;
                        bitsArray[bitIndex++] = (green >> i) & 1;
                        bitsArray[bitIndex++] = (blue >> i) & 1;
                    }
                }
            }
            return bitsArray;
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
        return bitStringBuilder.toString().chars().map(c -> c - '0').toArray();
    }

    public static StringBuilder getStringFromBits(int[] bits) {
        StringBuilder decodedText = new StringBuilder();
        for (int i = 0; i + 8 <= bits.length; i += 8) {
            String byteStr = Arrays.stream(Arrays.copyOfRange(bits, i, i + 8))
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                    .toString();
            decodedText.append((char) Integer.parseInt(byteStr, 2));
        }
        return decodedText;
    }

    public void writeImage(int[] decodedEncodedBits, String outputPath) {
        try {
            if (inputImage == null) {
                System.out.println("No input image is loaded.");
                return;
            }

            int width = inputImage.getWidth();
            int height = inputImage.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            List<Integer> rgbList = getRgbList(decodedEncodedBits);

            int pixelIndex = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int red = rgbList.get(pixelIndex++);
                    int green = rgbList.get(pixelIndex++);
                    int blue = rgbList.get(pixelIndex++);
                    image.setRGB(x, y, (red << 16) | (green << 8) | blue);
                }
            }

            ImageIO.write(image, "png", new File(outputPath));
            System.out.println("Image written successfully to " + outputPath);

        } catch (IOException e) {
            System.out.println("Error writing image: " + e.getMessage());
        }
    }

    private List<Integer> getRgbList(int[] decodedEncodedBits) {
        List<Integer> rgbList = new ArrayList<>();
        for (int i = 0; i + 24 <= decodedEncodedBits.length; i += 24) {
            int red = 0, green = 0, blue = 0;

            for (int j = 0; j < 8; j++) {
                red = (red << 1) | decodedEncodedBits[i + j];
                green = (green << 1) | decodedEncodedBits[i + 8 + j];
                blue = (blue << 1) | decodedEncodedBits[i + 16 + j];
            }

            rgbList.add(Math.min(255, Math.max(0, red)));
            rgbList.add(Math.min(255, Math.max(0, green)));
            rgbList.add(Math.min(255, Math.max(0, blue)));
        }
        return rgbList;
    }
}
