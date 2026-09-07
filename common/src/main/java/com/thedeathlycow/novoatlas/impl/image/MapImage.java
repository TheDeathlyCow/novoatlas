package com.thedeathlycow.novoatlas.impl.image;

import net.minecraft.util.Mth;
import org.joml.Vector2fc;

public abstract class MapImage {
    private final int width;
    private final int height;

    protected MapImage(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public final int sample(int x, int z, MapInfo info) {
        double horizontalScale = info.horizontalScale().value();
        Vector2fc centerOffset = info.centerOffset();

        x += (int) (-centerOffset.x() * this.width);
        z += (int) (-centerOffset.y() * this.height);

        double xR = (x / horizontalScale) + this.width / 2.0; // these will always be even numbers
        double zR = (z / horizontalScale) + this.height / 2.0;

        return this.sampleInterpolated(xR, zR, info);
    }

    public final int width() {
        return width;
    }

    public final int height() {
        return height;
    }

    public final int getTruncated(double x, double z, EdgeHandling edgeHandling) {
        int truncatedX = Mth.floor(x);
        int truncatedZ = Mth.floor(z);
        return this.getPixelValue(truncatedX, truncatedZ, edgeHandling);
    }

    public final int getPixelValue(int x, int z, EdgeHandling edgeHandling) {
        return edgeHandling.transform(x, z, this.width, this.height, this::getPixelValue);
    }

    protected abstract int getPixelValue(int x, int z);

    protected abstract int sampleInterpolated(double x, double z, MapInfo info);
}