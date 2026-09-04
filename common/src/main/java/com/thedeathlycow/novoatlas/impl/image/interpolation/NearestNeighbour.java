package com.thedeathlycow.novoatlas.impl.image.interpolation;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.image.MapImage;

public final class NearestNeighbour implements Interpolator {
    public static final MapCodec<NearestNeighbour> CODEC = MapCodec.unit(new NearestNeighbour());

    @Override
    public double sample(double x, double z, MapImage image) {
        return image.getTruncated(x, z);
    }

    @Override
    public MapCodec<NearestNeighbour> codec() {
        return CODEC;
    }
}