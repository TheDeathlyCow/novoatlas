package com.thedeathlycow.novoatlas.impl.image;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public final class HeightMapImage extends MapImage {
    private final short[][] pixels;

    public HeightMapImage(int width, int height, short[][] pixels) {
        super(width, height);
        this.pixels = pixels;
    }

    public static HeightMapImage fromBufferedImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        short[][] pixels = getGrayScalePixels(image, width, height);

        return new HeightMapImage(width, height, pixels);
    }

    private static short[][] getGrayScalePixels(BufferedImage image, int width, int height) {
        short[][] pixels = new short[width][height];
        Raster raster = image.getRaster();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[x][y] = (short) raster.getSample(x, y, 0);
            }
        }

        return pixels;
    }

    @Override
    public int getPixelValue(int x, int z) {
        return this.pixels[x][z];
    }

    @Override
    protected int sampleInterpolated(double x, double z, MapInfo info) {
        double height = info.horizontalScale().sample(x, z, this);
        return (int) Math.round(info.verticalScale() * height + info.startingY());
    }
}