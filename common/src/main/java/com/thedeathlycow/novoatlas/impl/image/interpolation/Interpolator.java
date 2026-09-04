package com.thedeathlycow.novoatlas.impl.image.interpolation;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.image.MapImage;
import com.thedeathlycow.novoatlas.impl.registry.NovoAtlasBuiltinRegistries;

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

    static Interpolator lanczos(int window) {
        Preconditions.checkArgument(window > 0, "Lanczos window must be positive");
        return new Lanczos(window);
    }
}