package com.thedeathlycow.novoatlas.impl.image;

import net.minecraft.util.Mth;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public abstract class MapImage {
    private final int width;
    private final int height;

    protected MapImage(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public final int sample(int x, int z, MapInfo info) {
        return this.sample(x, z, info, Integer.MIN_VALUE);
    }

    public final int sample(int x, int z, MapInfo info, int fallback) {
        double horizontalScale = info.horizontalScale().value();
        double xR = (x / horizontalScale) + this.width / 2.0; // these will always be even numbers
        double zR = (z / horizontalScale) + this.height / 2.0;

        if (xR < 0 || zR < 0 || xR >= this.width || zR >= this.height) {
            return fallback;
        }

        return this.sampleInterpolated(xR, zR, info);
    }

    public final int getTruncated(double x, double z) {
        int truncatedX = Mth.floor(x);
        int truncatedZ = Mth.floor(z);
        return this.getPixelValue(truncatedX, truncatedZ);
    }

    public final int width() {
        return width;
    }

    public final int height() {
        return height;
    }

    public abstract int getPixelValue(int x, int z);

    protected abstract int sampleInterpolated(double x, double z, MapInfo info);

}