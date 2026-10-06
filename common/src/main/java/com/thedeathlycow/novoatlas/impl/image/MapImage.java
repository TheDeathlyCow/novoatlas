package com.thedeathlycow.novoatlas.impl.image;

import net.minecraft.util.Mth;
import org.joml.Vector2d;
import org.joml.Vector2fc;

public abstract class MapImage {
    private final int width;
    private final int height;

    protected MapImage(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public final double getDistanceToEdge(int x, int z, MapInfo info) {
        float horizontalScale = info.horizontalScale().value();
        Vector2fc centerOffset = info.centerOffset();

        Vector2d p = new Vector2d(
                getIndexWithAlpha(x, horizontalScale, centerOffset.x(), this.width),
                getIndexWithAlpha(z, horizontalScale, centerOffset.y(), this.height)
        );

        Vector2d clampedEdgePoint = new Vector2d(
                Mth.clamp(p.x(), 0, this.width),
                Mth.clamp(p.y(), 0, this.height)
        );

        return p.distance(clampedEdgePoint);
    }

    public final boolean isBlockInsideImage(int x, int z, MapInfo info) {
        double horizontalScale = info.horizontalScale().value();
        Vector2fc centerOffset = info.centerOffset();

        double xR = getIndexWithAlpha(x, horizontalScale, centerOffset.x(), this.width);
        double zR = getIndexWithAlpha(z, horizontalScale, centerOffset.y(), this.height);

        return xR >= 0 && xR <= this.width && zR >= 0 && zR <= this.height;
    }

    public final int minBlockX(MapInfo info) {
        return Mth.floor(indexToBlock(0, info.horizontalScale().value(), info.centerOffset().x(), this.width));
    }

    public final int minBlockZ(MapInfo info) {
        return Mth.floor(indexToBlock(0, info.horizontalScale().value(), info.centerOffset().y(), this.height));
    }

    public final int maxBlockX(MapInfo info) {
        return Mth.floor(indexToBlock(this.width, info.horizontalScale().value(), info.centerOffset().x(), this.width));
    }

    public final int maxBlockZ(MapInfo info) {
        return Mth.floor(indexToBlock(this.height, info.horizontalScale().value(), info.centerOffset().y(), this.height));
    }

    public final int sample(int x, int z, MapInfo info) {
        double horizontalScale = info.horizontalScale().value();
        Vector2fc centerOffset = info.centerOffset();

        double xR = getIndexWithAlpha(x, horizontalScale, centerOffset.x(), this.width);
        double zR = getIndexWithAlpha(z, horizontalScale, centerOffset.y(), this.height);

        return this.sampleInterpolated(xR, zR, info);
    }

    private static double getIndexWithAlpha(int block, double horizontalScale, double centerOffset, double size) {
        double iR = block - (centerOffset * size);
        return (iR / horizontalScale) + size * 0.5;
    }

    public static double indexToBlock(int index, double horizontalScale, double centerOffset, double size) {
        return horizontalScale * (index - (size * 0.5)) + centerOffset * size;
    }

    public final int width() {
        return width;
    }

    public final int height() {
        return height;
    }

    public final int getTruncated(double x, double z, ImageWrapping imageWrapping) {
        int truncatedX = Mth.floor(x);
        int truncatedZ = Mth.floor(z);
        return this.getPixelValue(truncatedX, truncatedZ, imageWrapping);
    }

    public final int getPixelValue(int x, int z, ImageWrapping imageWrapping) {
        return imageWrapping.transform(x, z, this.width, this.height, this::getPixelValue);
    }

    protected abstract int getPixelValue(int x, int z);

    protected abstract int sampleInterpolated(double x, double z, MapInfo info);
}