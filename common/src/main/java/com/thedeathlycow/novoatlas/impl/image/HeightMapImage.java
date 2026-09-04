package com.thedeathlycow.novoatlas.impl.image;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public final class HeightMapImage extends MapImage {
    // char used as an unsigned 16 bit value
    private final char[] pixels;

    public HeightMapImage(int width, int height, char[] pixels) {
        super(width, height);
        this.pixels = pixels;
    }

    public static HeightMapImage fromBufferedImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        char[] pixels = getGrayScalePixels(image, width, height);

        return new HeightMapImage(width, height, pixels);
    }

    private static char[] getGrayScalePixels(BufferedImage image, int width, int height) {
        char[] pixels = new char[width * height];
        Raster raster = image.getRaster();

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                pixels[z * width + x] = (char) raster.getSample(x, z, 0);
            }
        }

        return pixels;
    }

    @Override
    public int getPixelValue(int x, int z) {
        return this.pixels[z * this.width() + x];
    }

    @Override
    protected int sampleInterpolated(double x, double z, MapInfo info) {
        double height = info.horizontalScale().sample(x, z, this);
        return (int) Math.round(info.verticalScale() * height + info.startingY());
    }
}