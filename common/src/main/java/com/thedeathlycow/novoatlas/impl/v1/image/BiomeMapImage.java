package com.thedeathlycow.novoatlas.impl.v1.image;

import com.thedeathlycow.novoatlas.impl.v2.image.data.ColorImageData;

import java.awt.image.BufferedImage;

public final class BiomeMapImage extends MapImage {
    private final int[] pixels;

    public BiomeMapImage(int width, int height, int[] pixels) {
        super(width, height);
        this.pixels = pixels;
    }

    public static BiomeMapImage fromBufferedImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = ColorImageData.getColorPixels(image, width, height);
        return new BiomeMapImage(width, height, pixels);
    }

    @Override
    public int getPixelValue(int x, int z) {
        return this.pixels[z * this.width() + x];
    }

    @Override
    protected int sampleInterpolated(double x, double z, MapInfo info) {
        return this.getTruncated(x, z, info.imageWrapping());
    }
}