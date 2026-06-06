package com.thedeathlycow.novoatlas.world.gen.interpolation;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.registry.NovoAtlasBuiltinRegistries;
import com.thedeathlycow.novoatlas.world.gen.MapImage;

import java.util.function.Function;

public interface Interpolator {
    MapCodec<Interpolator> BASE_CODEC = NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE.byNameCodec()
            .dispatchMap(
                    "interpolation",
                    Interpolator::codec,
                    Function.identity()
            );

    double sample(double x, double z, MapImage image);

    MapCodec<? extends Interpolator> codec();

    static Interpolator nearestNeighbour() {
        return new NearestNeighbour();
    }

    static Interpolator bilinear() {
        return new Bilinear();
    }

    static Interpolator bicubic() {
        return new Bicubic();
    }
}