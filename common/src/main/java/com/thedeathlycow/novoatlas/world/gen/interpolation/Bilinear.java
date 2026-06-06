package com.thedeathlycow.novoatlas.world.gen.interpolation;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.world.gen.MapImage;
import net.minecraft.util.Mth;

public final class Bilinear implements Interpolator {
    public static final MapCodec<Bilinear> CODEC = MapCodec.unit(new Bilinear());

    @Override
    public double sample(double x, double z, MapImage image) {
        // x and z are the truncated (floor) coordinates
        // deltaX and deltaZ are the fractional parts
        int truncatedX = Mth.floor(x);
        int truncatedZ = Mth.floor(z);

        // x - truncatedX gets the fractional part of the sampled point, to use for lerp deltas
        double deltaX = x - truncatedX;
        double deltaZ = z - truncatedZ;

        int nextX = Math.min(truncatedX + 1, image.width() - 1);
        int nextZ = Math.min(truncatedZ + 1, image.height() - 1);

        int[][] pixels = image.pixels();

        int topLeft = pixels[truncatedX][truncatedZ];
        int topRight = pixels[nextX][truncatedZ];
        int bottomLeft = pixels[truncatedX][nextZ];
        int bottomRight = pixels[nextX][nextZ];

        return Mth.lerp2(deltaX, deltaZ, topLeft, topRight, bottomLeft, bottomRight);
    }

    @Override
    public MapCodec<Bilinear> codec() {
        return CODEC;
    }
}