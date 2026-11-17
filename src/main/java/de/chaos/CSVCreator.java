package de.chaos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVCreator {

    private CSVCreator() {
        throw new UnsupportedOperationException("Utility class");
    }


    public static void writePixelsToCSV(java.util.List<ImagePixel> pixels, String outputPath) throws IOException {
        String csvPath = outputPath.replace(".png", ".csv").replace(".jpg", ".csv").replace(".jpeg", ".csv");
        File file = new File(csvPath);
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write header
            writer.write("x,y,colourType,weighted");
            writer.newLine();

            // Write each pixel
            for (ImagePixel pixel : pixels) {
                writer.write(String.format("%d,%d,%s,%s",
                        pixel.getX(), pixel.getY(), pixel.getColourType(), pixel.isWeighted()));
                writer.newLine();
            }
        }
        System.out.println("CSV saved to: " + outputPath);
    }
}
