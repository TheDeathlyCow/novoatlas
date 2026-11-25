package com.thedeathlycow.novoatlas.impl.v2.image.interpolation;

import com.mojang.serialization.MapCodec;

public final class NearestNeighbour implements Interpolator {
    public static final MapCodec<NearestNeighbour> CODEC = MapCodec.unit(new NearestNeighbour());

    @Override
    public double sample(double x, double z, Context ctx) {
        return ctx.image().getTruncated(x, z, ctx.wrapping());
    }

    @Override
    public MapCodec<NearestNeighbour> codec() {
        return CODEC;
    }
}