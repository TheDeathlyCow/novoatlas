package com.thedeathlycow.novoatlas.impl.v1.image.interpolation;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.v1.image.MapImage;
import com.thedeathlycow.novoatlas.impl.v1.image.MapInfo;

public final class NearestNeighbour implements Interpolator {
    public static final MapCodec<NearestNeighbour> CODEC = MapCodec.unit(new NearestNeighbour());

    @Override
    public double sample(double pixelX, double pixelZ, MapImage image, MapInfo mapInfo) {
        return image.getTruncated(pixelX, pixelZ, mapInfo.imageWrapping());
    }

    @Override
    public MapCodec<NearestNeighbour> codec() {
        return CODEC;
    }
}