package com.thedeathlycow.novoatlas.impl.v2.image.interpolation;

import com.mojang.serialization.MapCodec;

public final class NearestNeighbour implements Interpolator {
    public static final MapCodec<NearestNeighbour> CODEC = MapCodec.unit(new NearestNeighbour());

    @Override
    public double sample(double pixelX, double pixelZ, Context ctx) {
        return ctx.getTruncated(pixelX, pixelZ);
    }

    @Override
    public MapCodec<NearestNeighbour> codec() {
        return CODEC;
    }
}