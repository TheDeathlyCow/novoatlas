package com.thedeathlycow.novoatlas.world.gen;

import net.minecraft.util.Mth;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public record MapImage(
        int width,
        int height,
        int[][] pixels,
        Type type
) {
    public enum Type {
        BIOME_MAP,
        HEIGHTMAP
    }

    public static MapImage fromBufferedImage(BufferedImage image, Type type) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[][] pixels = type == Type.BIOME_MAP ? getColorPixels(image, width, height) : getGrayScalePixels(image, width, height);

        return new MapImage(width, height, pixels, type);
    }

    private static int[][] getGrayScalePixels(BufferedImage image, int width, int height) {
        int[][] pixels = new int[width][height];
        Raster raster = image.getRaster();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[x][y] = raster.getSample(x, y, 0);
            }
        }

        return pixels;
    }

    private static int[][] getColorPixels(BufferedImage image, int width, int height) {
        int[] data = new int[width * height];
        image.getRGB(0, 0, width, height, data, 0, width);

        int x = 0;
        int y = 0;
        int[][] pixels = new int[width][height];

        for (int datum : data) {
            if (x >= width) {
                x = 0;
                y++;
            }
            pixels[x++][y] = datum & 0xffffff;
        }

        return pixels;
    }

    public int sample(int x, int z, MapInfo info) {
        return this.sample(x, z, info, Integer.MIN_VALUE);
    }

    public int sample(int x, int z, MapInfo info, int fallback) {
        double horizontalScale = info.horizontalScale();
        double xR = (x / horizontalScale) + this.width() / 2.0; // these will always be even numbers
        double zR = (z / horizontalScale) + this.height() / 2.0;

        if (xR < 0 || zR < 0 || xR >= this.width() || zR >= this.height()) {
            return fallback;
        }

        int truncatedX = Mth.floor(xR);
        int truncatedZ = Mth.floor(zR);

        // xR - truncatedX gets the fractional part of the sampled point, to use for lerp deltas
        double deltaX = xR - truncatedX;
        double deltaZ = zR - truncatedZ;

        if (this.type == Type.HEIGHTMAP) {
            double height = this.bilerp(truncatedX, deltaX, truncatedZ, deltaZ, this.type);
            return Mth.floor(info.verticalScale() * height + info.startingY());
        } else { // BIOME_MAP
            double color = this.bilerp(truncatedX, deltaX, truncatedZ, deltaZ, this.type);
            return (int) color;
        }
    }

    private double bilerp(int x, double deltaX, int z, double deltaZ, Type type) {
        // x and z are the truncated (floor) coordinates
        // deltaX and deltaZ are the fractional parts

        // Get the four corner pixel values
        double i00 = pixels[x][z];
        double i01 = pixels[Math.min(x + 1, width - 1)][z]; // Value at (x+1, z)
        double i10 = pixels[x][Math.min(z + 1, height - 1)]; // Value at (x, z+1)
        double i11 = pixels[Math.min(x + 1, width - 1)][Math.min(z + 1, height - 1)]; // Value at (x+1, z+1)

        if (type == Type.HEIGHTMAP) {
            return Mth.lerp2(deltaX, deltaZ, i00, i01, i10, i11);
        } else { // BIOME_MAP
            // Interpolate each color channel separately
            double r = Mth.lerp2(deltaX, deltaZ, getRed(i00), getRed(i01), getRed(i10), getRed(i11));
            double g = Mth.lerp2(deltaX, deltaZ, getGreen(i00), getGreen(i01), getGreen(i10), getGreen(i11));
            double b = Mth.lerp2(deltaX, deltaZ, getBlue(i00), getBlue(i01), getBlue(i10), getBlue(i11));
            int interpolatedColor = rgbToInt((int) r, (int) g, (int) b);
            return findClosestColor(interpolatedColor, (int) i00, (int) i01, (int) i10, (int) i11);
        }
    }

    private int getRed(double rgb) {
        return ((int) rgb >> 16) & 0xFF;
    }

    private int getGreen(double rgb) {
        return ((int) rgb >> 8) & 0xFF;
    }

    private int getBlue(double rgb) {
        return (int) rgb & 0xFF;
    }

    private int rgbToInt(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    private static double colorDistanceSq(int c1, int c2) {
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;

        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;

        return Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2);
    }

    private static int findClosestColor(int targetColor, int... colors) {
        int closestColor = colors[0];
        double minDistance = colorDistanceSq(targetColor, closestColor);

        for (int i = 1; i < colors.length; i++) {
            double distance = colorDistanceSq(targetColor, colors[i]);
            if (distance < minDistance) {
                minDistance = distance;
                closestColor = colors[i];
            }
        }
        return closestColor;
    }
}