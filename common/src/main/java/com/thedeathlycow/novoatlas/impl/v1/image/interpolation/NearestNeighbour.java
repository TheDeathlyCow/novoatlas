package com.thedeathlycow.novoatlas.impl.v1.image.interpolation;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.v1.image.MapImage;
import com.thedeathlycow.novoatlas.impl.v1.image.MapInfo;

public final class NearestNeighbour implements Interpolator {
    public static final MapCodec<NearestNeighbour> CODEC = MapCodec.unit(new NearestNeighbour());

    @Override
    public double sample(double x, double z, MapImage image, MapInfo mapInfo) {
        return image.getTruncated(x, z, mapInfo.imageWrapping());
    }

    @Override
    public MapCodec<NearestNeighbour> codec() {
        return CODEC;
    }
}