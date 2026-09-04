package com.thedeathlycow.novoatlas.impl.image;

import java.awt.image.BufferedImage;

public final class BiomeMapImage extends MapImage {
    private final int[][] pixels;

    public BiomeMapImage(int width, int height, int[][] pixels) {
        super(width, height);
        this.pixels = pixels;
    }

    public static BiomeMapImage fromBufferedImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[][] pixels = getColorPixels(image, width, height);

        return new BiomeMapImage(width, height, pixels);
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

    @Override
    public int getPixelValue(int x, int z) {
        return this.pixels[x][z];
    }

    @Override
    protected int sampleInterpolated(double x, double z, MapInfo info) {
        return this.getTruncated(x, z);
    }
}