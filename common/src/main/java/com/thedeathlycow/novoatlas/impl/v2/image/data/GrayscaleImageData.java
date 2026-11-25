package com.thedeathlycow.novoatlas.impl.v2.image.data;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

/// Stores 16-bit grayscale image data.
public final class GrayscaleImageData implements ImageData {
    private final int width;
    private final int height;
    // char used as an unsigned 16 bit value
    private final char[] pixels;

    public GrayscaleImageData(int width, int height, char[] pixels) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }

    @Override
    public int getPixelValue(int x, int z) {
        // the 16 bit value would occupy 32 bits of stack memory here anyway so returning as an int has no performance
        // penalty.
        return this.pixels[z * this.width() + x];
    }

    public static GrayscaleImageData fromBufferedImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        char[] pixels = getGrayScalePixels(image, width, height);

        return new GrayscaleImageData(width, height, pixels);
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
}