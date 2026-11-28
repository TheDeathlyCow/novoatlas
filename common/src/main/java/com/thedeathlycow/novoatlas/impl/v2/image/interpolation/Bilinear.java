package com.thedeathlycow.novoatlas.impl.v2.image.interpolation;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.Mth;

public final class Bilinear implements Interpolator {
    static final Bilinear INSTANCE = new Bilinear();
    public static final MapCodec<Bilinear> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public double sample(double pixelX, double pixelZ, Context ctx) {
        // x and z are the truncated (floor) coordinates
        // deltaX and deltaZ are the fractional parts
        int truncatedX = Mth.floor(pixelX);
        int truncatedZ = Mth.floor(pixelZ);

        // x - truncatedX gets the fractional part of the sampled point, to use for lerp deltas
        double deltaX = pixelX - truncatedX;
        double deltaZ = pixelZ - truncatedZ;

        int nextX = truncatedX + 1;
        int nextZ = truncatedZ + 1;

        int topLeft = ctx.getWrappedPixelValue(truncatedX, truncatedZ);
        int topRight = ctx.getWrappedPixelValue(nextX, truncatedZ);
        int bottomLeft = ctx.getWrappedPixelValue(truncatedX, nextZ);
        int bottomRight = ctx.getWrappedPixelValue(nextX, nextZ);

        return Mth.lerp2(deltaX, deltaZ, topLeft, topRight, bottomLeft, bottomRight);
    }

    @Override
    public MapCodec<Bilinear> codec() {
        return CODEC;
    }

    private Bilinear() {

    }
}