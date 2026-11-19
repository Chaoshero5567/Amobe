package de.chaos;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class PixelCounter {

    String resourceDir = "src/main/resources";
    File dir;
    int counter = 0;

    // Laden der Bilder
    private static BufferedImage loadImage(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("Bild net gefunden: " + path);
        }
        return ImageIO.read(file);
    }

    // Alles
     private List<ImagePixel> analyzeAndSave(BufferedImage image, String name, String outputPath) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();

        boolean[][] isAmoeba = new boolean[height][width];
        boolean[][] isYellowPixel = new boolean[height][width];

        long yellowPixels = 0;
        long whitePixels = 0;
        double sumX = 0;
        double sumY = 0;

        int stickerMinX = Integer.MAX_VALUE;
        int stickerMinY = Integer.MAX_VALUE;
        int stickerMaxX = Integer.MIN_VALUE;
        int stickerMaxY = Integer.MIN_VALUE;
        boolean hasSticker = false;

        // Listen für Outline und aller Analysierten Pixel
        List<ImagePixel> allPixels = new ArrayList<>();
        List<String> outlinePoints = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                // Check auf Transparenz, 128 wurde genommen da es den Mittelpunkt darstellt und so den Outline etwas genauer bestimmen sollte.
                if (alpha < 128) {
                    continue;
                }

                // Fragt ab ob die Pixel Gelb sind, also teil der Haupt Amöbe
                if (isYellow(red, green, blue)) {
                    yellowPixels++;
                    isAmoeba[y][x] = true;
                    isYellowPixel[y][x] = true;

                    // Daten werden hinzugefügt
                    allPixels.add(new ImagePixel(x, y, "yellow", true, 1.0d));
                } else if (isWhiteSticker(red, green, blue)) {
                    whitePixels++;
                    isAmoeba[y][x] = true;
                    hasSticker = true;
                    if (x < stickerMinX) stickerMinX = x;
                    if (x > stickerMaxX) stickerMaxX = x;
                    if (y < stickerMinY) stickerMinY = y;
                    if (y > stickerMaxY) stickerMaxY = y;
                    // abfrage der Sticker area
                    allPixels.add(new ImagePixel(x, y, "sticker", true, 1.01d));
                } else {
                    // Pixel werden als Hintergrund gezählt
                    allPixels.add(new ImagePixel(x, y, "background", false, 0.0d));
                }
            }
        }


        // redunanz abfrage
        if (hasSticker) {
            int expand = 5;
            int sMinX = Math.max(0, stickerMinX - expand);
            int sMinY = Math.max(0, stickerMinY - expand);
            int sMaxX = Math.min(width - 1, stickerMaxX + expand);
            int sMaxY = Math.min(height - 1, stickerMaxY + expand);
            for (int y = sMinY; y <= sMaxY; y++) {
                for (int x = sMinX; x <= sMaxX; x++) {
                    if (!isAmoeba[y][x]) {
                        isAmoeba[y][x] = true;
                        whitePixels++;
                        allPixels.add(new ImagePixel(x, y, "sticker", true, 1.01d));
                    }
                }
            }
        }


         double sumWeight = 0;

         // Sticker gewicht berechnung
         for (ImagePixel pixel : allPixels) {
             if (pixel.isWeighted()) {
                 double w = pixel.getWeight();
                 sumX += pixel.getX() * w;
                 sumY += pixel.getY() * w;
                 sumWeight += w;
             }
         }

         double xCenter = sumX / sumWeight;
         double yCenter = sumY / sumWeight;

        // edge
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!isAmoeba[y][x]) continue;

                boolean isEdge = false;
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    isEdge = true;
                } else {
                    if (!isAmoeba[y - 1][x] || !isAmoeba[y + 1][x] ||
                            !isAmoeba[y][x - 1] || !isAmoeba[y][x + 1]) {
                        isEdge = true;
                    }
                }

                if (isEdge) {
                    outlinePoints.add("(" + x + ", " + y + ")");
                }
            }
        }

        System.out.println("--- " + name + " ---");
        System.out.println("Schwerpunkt liegt bei: (" + String.format("%.3f", xCenter) + "; " + String.format("%.3f", yCenter) + ")");

        StatisticMomentum.MomentumResult momentum = StatisticMomentum.berechne(allPixels);

        // Bild generation
        BufferedImage result = drawResult(image, isAmoeba, xCenter, yCenter, momentum);

        File outputFile = new File(outputPath);
        outputFile.getParentFile().mkdirs();
        ImageIO.write(result, "png", outputFile);
        System.out.println("ergebnis gespeichert unter: " + outputPath);


        return allPixels;
    }

    // Ist es gelb?
    static boolean isYellow(int r, int g, int b) {
        if (r < 150 || g < 150) {
            return false;
        }
        if (b > 150) {
            return false;
        }
        int dominant = Math.max(r, g);
        int secondary = Math.min(r, g);
        return dominant - secondary <= 80;
    }

    // ist es der sticker
    static boolean isWhiteSticker(int r, int g, int b) {
        if (r < 200 || g < 200 || b < 200) {
            return false;
        }
        if (r > 250 && g > 250 && b > 250) {
            return false;
        }
        int maxChannel = Math.max(r, Math.max(g, b));
        int minChannel = Math.min(r, Math.min(g, b));
        return maxChannel - minChannel <= 30;
    }

    static BufferedImage drawResult(BufferedImage image, boolean[][] isAmoeba, double centerX, double centerY, StatisticMomentum.MomentumResult momentum) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();

        int height = isAmoeba.length;
        int width = isAmoeba[0].length;

        // Füllt Die area der Amöbe pixel
        g2d.setColor(Color.WHITE);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isAmoeba[y][x]) {
                    g2d.fillRect(x, y, 1, 1);
                }
            }
        }

        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2f));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!isAmoeba[y][x]) continue;

                boolean isEdge = false;
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    isEdge = true;
                } else {
                    if (!isAmoeba[y - 1][x] || !isAmoeba[y + 1][x] ||
                            !isAmoeba[y][x - 1] || !isAmoeba[y][x + 1]) {
                        isEdge = true;
                    }
                }

                if (isEdge) {
                    g2d.fillRect(x, y, 1, 1);
                }
            }
        }

        g2d.setColor(Color.RED);
        int radius = 8;
        g2d.fillOval((int) centerX - radius, (int) centerY - radius, radius * 2, radius * 2);


        // Momentum Feile
        if (momentum != null) {
            g2d.setColor(Color.BLUE);
            g2d.fillOval(momentum.centerX - radius, momentum.centerY - radius, radius * 2, radius * 2);

            g2d.setColor(new Color(0, 255, 0, 180));
            g2d.setStroke(new BasicStroke(3f));
            g2d.drawLine(momentum.centerX, momentum.centerY,
                    (int) (momentum.centerX + momentum.xRadius), momentum.centerY);

            g2d.setColor(new Color(255, 0, 255, 180));
            g2d.drawLine(momentum.centerX, momentum.centerY,
                    momentum.centerX, (int) (momentum.centerY + momentum.yRadius));
        }

        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine((int) centerX - 20, (int) centerY, (int) centerX + 20, (int) centerY);
        g2d.drawLine((int) centerX, (int) centerY - 20, (int) centerX, (int) centerY + 20);

        // text
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        String label = String.format("CM: (%.3f, %.3f)", centerX, centerY);
        g2d.drawString(label, (int) centerX + 20, (int) centerY - 20);

        g2d.dispose();
        return result;
    }

    public static void main(String[] args) {
        new PixelCounter().run(args);
    }

    void run(String[] args) {
        try {
            System.out.println("=== Amoeba Analysis ===\n");

            this.dir = new File(this.resourceDir);
            File[] imageFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));

            if (imageFiles == null || imageFiles.length == 0) {
                System.err.println("keine bildchen gefunden in: " + resourceDir);
                return;
            }

            Arrays.sort(imageFiles);

            for (int i = 0; i < imageFiles.length; i++) {
                String fileName = imageFiles[i].getName();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                BufferedImage image = loadImage(imageFiles[i].getPath());

                List<ImagePixel> pixels = analyzeAndSave(image, baseName, "output/" + baseName + "_result.png");
                CSVCreator.writePixelsToCSV(pixels, "output/" + baseName + "_result.png");

                if (i < imageFiles.length - 1) {
                    System.out.println();
                }
            }

        } catch (IOException e) {
            System.err.println("Käfer: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
