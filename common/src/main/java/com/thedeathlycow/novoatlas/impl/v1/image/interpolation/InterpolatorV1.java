package com.thedeathlycow.novoatlas.impl.v1.image.interpolation;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.v1.image.MapImage;
import com.thedeathlycow.novoatlas.impl.v1.image.MapInfo;
import com.thedeathlycow.novoatlas.impl.v1.registry.NovoAtlasBuiltinRegistriesV1;

import java.util.function.Function;

public interface InterpolatorV1 {
    MapCodec<InterpolatorV1> BASE_CODEC = NovoAtlasBuiltinRegistriesV1.INTERPOLATOR_TYPE.byNameCodec()
            .dispatchMap(
                    "interpolation",
                    InterpolatorV1::codec,
                    Function.identity()
            );

    double sample(double x, double z, MapImage image, MapInfo mapInfo);

    MapCodec<? extends InterpolatorV1> codec();

    static InterpolatorV1 nearestNeighbour() {
        return new NearestNeighbourV1();
    }

    static InterpolatorV1 bilinear() {
        return new BilinearV1();
    }

    static InterpolatorV1 bicubic() {
        return new BicubicV1();
    }

    static InterpolatorV1 lanczos(int window) {
        Preconditions.checkArgument(window > 0, "Lanczos window must be positive");
        return new LanczosV1(window);
    }
}