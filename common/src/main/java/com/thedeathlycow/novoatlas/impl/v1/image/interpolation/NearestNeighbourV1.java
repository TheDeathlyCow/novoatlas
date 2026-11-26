package com.thedeathlycow.novoatlas.impl.v1.image.interpolation;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.v1.image.MapImage;
import com.thedeathlycow.novoatlas.impl.v1.image.MapInfo;

public final class NearestNeighbourV1 implements InterpolatorV1 {
    public static final MapCodec<NearestNeighbourV1> CODEC = MapCodec.unit(new NearestNeighbourV1());

    @Override
    public double sample(double pixelX, double pixelZ, MapImage image, MapInfo mapInfo) {
        return image.getTruncated(pixelX, pixelZ, mapInfo.imageWrapping());
    }

    @Override
    public MapCodec<NearestNeighbourV1> codec() {
        return CODEC;
    }
}