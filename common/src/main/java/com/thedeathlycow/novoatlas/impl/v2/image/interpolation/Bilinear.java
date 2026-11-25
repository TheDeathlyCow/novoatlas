package com.thedeathlycow.novoatlas.impl.v2.image.interpolation;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.v2.image.ImageWrapping;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ImageData;
import net.minecraft.util.Mth;

public final class Bilinear implements Interpolator {
    public static final MapCodec<Bilinear> CODEC = MapCodec.unit(new Bilinear());

    @Override
    public double sample(double x, double z, Context ctx) {
        // x and z are the truncated (floor) coordinates
        // deltaX and deltaZ are the fractional parts
        int truncatedX = Mth.floor(x);
        int truncatedZ = Mth.floor(z);

        // x - truncatedX gets the fractional part of the sampled point, to use for lerp deltas
        double deltaX = x - truncatedX;
        double deltaZ = z - truncatedZ;

        int nextX = truncatedX + 1;
        int nextZ = truncatedZ + 1;

        ImageData image = ctx.image();
        ImageWrapping wrapping = ctx.wrapping();

        int topLeft = image.getWrappedPixelValue(truncatedX, truncatedZ, wrapping);
        int topRight = image.getWrappedPixelValue(nextX, truncatedZ, wrapping);
        int bottomLeft = image.getWrappedPixelValue(truncatedX, nextZ, wrapping);
        int bottomRight = image.getWrappedPixelValue(nextX, nextZ, wrapping);

        return Mth.lerp2(deltaX, deltaZ, topLeft, topRight, bottomLeft, bottomRight);
    }

    @Override
    public MapCodec<Bilinear> codec() {
        return CODEC;
    }
}