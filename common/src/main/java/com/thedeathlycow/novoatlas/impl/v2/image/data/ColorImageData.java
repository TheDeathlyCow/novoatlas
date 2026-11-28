package com.thedeathlycow.novoatlas.impl.v2.image.data;

import java.awt.image.BufferedImage;

/// Stores 32-bit (8 bit per channel) RGB color data.
public final class ColorImageData implements ImageData {
    private final int width;
    private final int height;
    private final int[] pixels;

    public ColorImageData(int width, int height, int[] pixels) {
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
        return this.pixels[z * this.width + x];
    }

    public static ColorImageData fromBufferedImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = getColorPixels(image, width, height);

        return new ColorImageData(width, height, pixels);
    }

    public static int[] getColorPixels(BufferedImage image, int width, int height) {
        int[] data = new int[width * height];
        image.getRGB(0, 0, width, height, data, 0, width);

        for (int i = 0; i < data.length; i++) {
            data[i] = data[i] & 0xffffff;
        }

        return data;
    }
}
