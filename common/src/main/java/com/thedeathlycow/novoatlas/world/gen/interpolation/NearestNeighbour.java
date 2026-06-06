package com.thedeathlycow.novoatlas.world.gen.interpolation;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.world.gen.MapImage;
import org.jspecify.annotations.NonNull;

final class NearestNeighbour implements Interpolator {
    @Override
    public double sample(double x, double z, MapImage image) {
        return image.getTruncated(x, z);
    }

    @Override
    public MapCodec<NearestNeighbour> codec() {
        return MapCodec.unit(new NearestNeighbour());
    }

    @Override
    @NonNull
    public String getSerializedName() {
        return "nearest_neighbor";
    }
}