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

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file), 64 * 1024)) {
            StringBuilder sb = new StringBuilder();

            sb.append("x,y,colourType,weighted").append('\n');

            for (ImagePixel pixel : pixels) {
                sb.append(pixel.getX()).append(',')
                        .append(pixel.getY()).append(',')
                        .append(pixel.getColourType()).append(',')
                        .append(pixel.isWeighted())
                        .append('\n');


            }
            writer.write(sb.toString());
        }
    }
}
