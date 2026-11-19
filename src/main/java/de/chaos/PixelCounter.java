package de.chaos;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PixelCounter {

    String resourceDir = "src/main/resources";
    File dir;

    void main() {
        new PixelCounter().run();
    }

    void run() {
        try {

            this.dir = new File(this.resourceDir);
            File[] imageFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));

            if (imageFiles == null || imageFiles.length == 0) {
                System.err.println("keine bildchen gefunden in: " + resourceDir);
                return;
            }

            Arrays.sort(imageFiles);

            //Paralele runtime für optimierung der Zeit (Hat fast 2 Sekunden gebracht)
            int parallel = Math.min(Runtime.getRuntime().availableProcessors(), imageFiles.length);
            ExecutorService executor = Executors.newFixedThreadPool(parallel);
            List<Future<ImageResult>> futures = new ArrayList<>();

            // Rechnen :3
            for (File file : imageFiles) {
                futures.add(executor.submit(() -> processImage(file)));
            }

            // Print out
            boolean first = true;
            for (Future<ImageResult> future : futures) {
                ImageResult result = future.get();
                if (!first) {

                }
                first = false;
                String output = result.getOutput().toString();
                System.out.println(output);
            }
            executor.shutdown();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private ImageResult processImage(File imageFile) {
        StringBuilder sb = new StringBuilder();
        try {
            String fileName = imageFile.getName();
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            BufferedImage image = loadImage(imageFile.getPath());

            List<ImagePixel> pixels = analyzeAndSave(image, baseName, "output/" + baseName + "_result.png");
            CSVCreator.writePixelsToCSV(pixels, "output/" + baseName + "_result.png");

            return new ImageResult(baseName, pixels, sb);
        } catch (IOException e) {
            sb.append("Käfer: ").append(e.getMessage()).append(System.lineSeparator());
            e.printStackTrace(new java.io.PrintWriter(new java.io.StringWriter()));
            return new ImageResult(imageFile.getName(), new ArrayList<>(), sb);
        }
    }

    // Laden der Bilder
    private BufferedImage loadImage(String path) throws IOException {
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

        double sumX = 0;
        double sumY = 0;
        double sumWeight = 0;

        int stickerMinX = Integer.MAX_VALUE;
        int stickerMinY = Integer.MAX_VALUE;
        int stickerMaxX = Integer.MIN_VALUE;
        int stickerMaxY = Integer.MIN_VALUE;
        boolean hasSticker = false;

        // Listen für Outline und aller analysierten Pixel
        List<ImagePixel> allPixels = new ArrayList<>(width * height);
        int[] rgbPixels = new int[width * height];
        image.getRGB(0, 0, width, height, rgbPixels, 0, width);

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = rgbPixels[index++];
                int alpha = (rgb >> 24) & 0xFF;
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                // Check auf Transparenz, 128 wurde genommen da es den Mittelpunkt darstellt und so den Outline etwas genauer bestimmen sollte.
                if (alpha < 128) {
                    continue;
                }

                // Fragt ab ob die Pixel Gelb sind, also teil der Haupt-Amöbe
                if (isYellow(red, green, blue)) {
                    isAmoeba[y][x] = true;

                    // Daten werden hinzugefügt
                    allPixels.add(new ImagePixel(x, y, "yellow", true, 1.0d));
                    sumX += x;
                    sumY += y;
                    sumWeight += 1.0d;
                } else if (isWhiteSticker(red, green, blue)) {
                    isAmoeba[y][x] = true;
                    hasSticker = true;
                    if (x < stickerMinX) stickerMinX = x;
                    if (x > stickerMaxX) stickerMaxX = x;
                    if (y < stickerMinY) stickerMinY = y;
                    if (y > stickerMaxY) stickerMaxY = y;
                    // abfrage der Sticker area
                    allPixels.add(new ImagePixel(x, y, "sticker", true, 1.01d));
                    sumX += x * 1.01d;
                    sumY += y * 1.01d;
                    sumWeight += 1.01d;
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
                        allPixels.add(new ImagePixel(x, y, "sticker", true, 1.01d));
                        sumX += x * 1.01d;
                        sumY += y * 1.01d;
                        sumWeight += 1.01d;
                    }
                }
            }
        }

        double xCenter = sumX / sumWeight;
        double yCenter = sumY / sumWeight;


        IO.println("Schwerpunkt liegt bei: (" + String.format("%.3f", xCenter) + "; " + String.format("%.3f", yCenter) + ")");

        StatisticMomentum.MomentumResult momentum = StatisticMomentum.berechne(allPixels);

        // Bild generation
        BufferedImage result = drawResult(image, isAmoeba, xCenter, yCenter, momentum);

        File outputFile = new File(outputPath);
        outputFile.getParentFile().mkdirs();
        ImageIO.write(result, "png", outputFile);


        return allPixels;
    }

    // Ist es gelb?
    boolean isYellow(int r, int g, int b) {
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
    boolean isWhiteSticker(int r, int g, int b) {
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

    BufferedImage drawResult(BufferedImage image, boolean[][] isAmoeba, double centerX, double centerY, StatisticMomentum.MomentumResult momentum) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        int height = isAmoeba.length;
        int width = isAmoeba[0].length;

        // Füllt Die area der Amöbe pixel und markiert Kanten in einem Durchgang
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
                    result.setRGB(x, y, 0xFFFF0000);
                } else {
                    result.setRGB(x, y, 0xFFFFFFFF);
                }
            }
        }

        Graphics2D g2d = result.createGraphics();
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2f));
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
}
