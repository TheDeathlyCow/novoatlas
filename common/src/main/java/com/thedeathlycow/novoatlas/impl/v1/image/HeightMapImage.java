package com.thedeathlycow.novoatlas.impl.v1.image;

import com.thedeathlycow.novoatlas.impl.v2.image.data.GrayscaleImageData;

import java.awt.image.BufferedImage;

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
        char[] pixels = GrayscaleImageData.getGrayScalePixels(image, width, height);
        return new HeightMapImage(width, height, pixels);
    }

    @Override
    public int getPixelValue(int x, int z) {
        return this.pixels[z * this.width() + x];
    }

    @Override
    protected int sampleInterpolated(double x, double z, MapInfo info) {
        double height = info.horizontalScale().sample(x, z, this, info);
        return (int) Math.round(info.verticalScale() * height + info.startingY());
    }
}