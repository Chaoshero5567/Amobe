package de.chaos;

import java.util.List;

public class StatisticMomentum {
    public static MomentumResult berechne(List<ImagePixel> pixels) {
        if (pixels == null || pixels.isEmpty()) {
            return null;
        }

        double sumX = 0;
        double sumY = 0;
        long count = 0;

        for (ImagePixel pixel : pixels) {
            if (pixel.isWeighted()) {
                sumX += pixel.getX();
                sumY += pixel.getY();
                count++;
            }
        }

        if (count == 0) {
            return null;
        }

        double xCenter = sumX / count;
        double yCenter = sumY / count;

        double x = 0.0;
        double y = 0.0;

        for (ImagePixel pixel : pixels) {
            if (pixel.isWeighted()) {
                double dx = pixel.getX() - xCenter;
                double dy = pixel.getY() - yCenter;

                x += dx * dx;
                y += dy * dy;
            }
        }

        System.out.println("Statische Momente:");
        System.out.println("x: " + x);
        System.out.println("y: " + y);

        double xRadius = Math.sqrt(x);
        double yRadius = Math.sqrt(y);

        return new MomentumResult((int) xCenter, (int) yCenter, xRadius, yRadius);
    }

    public static class MomentumResult {
        final int centerX;
        final int centerY;
        final double xRadius;
        final double yRadius;

        MomentumResult(int cx, int cy, double xr, double yr) {
            this.centerX = cx;
            this.centerY = cy;
            this.xRadius = xr;
            this.yRadius = yr;
        }
    }
}
