package com.thedeathlycow.novoatlas.impl.image.interpolation;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.image.MapImage;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.util.Mth;

public final class Bilinear implements Interpolator {
    public static final MapCodec<Bilinear> CODEC = MapCodec.unit(new Bilinear());

    @Override
    public double sample(double x, double z, MapImage image, MapInfo mapInfo) {
        // x and z are the truncated (floor) coordinates
        // deltaX and deltaZ are the fractional parts
        int truncatedX = Mth.floor(x);
        int truncatedZ = Mth.floor(z);

        // x - truncatedX gets the fractional part of the sampled point, to use for lerp deltas
        double deltaX = x - truncatedX;
        double deltaZ = z - truncatedZ;

        int nextX = Math.min(truncatedX + 1, image.width() - 1);
        int nextZ = Math.min(truncatedZ + 1, image.height() - 1);

        int topLeft = image.getPixelValue(truncatedX, truncatedZ, mapInfo.imageWrapping());
        int topRight = image.getPixelValue(nextX, truncatedZ, mapInfo.imageWrapping());
        int bottomLeft = image.getPixelValue(truncatedX, nextZ, mapInfo.imageWrapping());
        int bottomRight = image.getPixelValue(nextX, nextZ, mapInfo.imageWrapping());

        return Mth.lerp2(deltaX, deltaZ, topLeft, topRight, bottomLeft, bottomRight);
    }

    @Override
    public MapCodec<Bilinear> codec() {
        return CODEC;
    }
}