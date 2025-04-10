package com.thedeathlycow.novoatlas.world.gen;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public record MapImage(
        int width,
        int height,
        int[][] pixels
) {
    public static MapImage fromBufferedImage(BufferedImage image, boolean color) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[][] pixels = color ? color(image, width, height) : grayscale(image, width, height);

        return new MapImage(width, height, pixels);
    }

    private static int[][] grayscale(BufferedImage image, int width, int height) {
        int[][] pixels = new int[width][height];
        Raster raster = image.getRaster();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[x][y] = raster.getSample(x, y, 0);
            }
        }

        return pixels;
    }

    private static int[][] color(BufferedImage image, int width, int height) {
        int[][] pixels = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[x][y] = image.getRGB(x, y);
            }
        }

        return pixels;
    }
}