package com.thedeathlycow.novoatlas.world.gen.interpolation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.world.gen.MapImage;
import net.minecraft.util.StringRepresentable;

public interface Interpolator extends StringRepresentable {
    Codec<Interpolator> BASE_CODEC = StringRepresentable.fromValues(() -> new Interpolator[]{
            nearestNeighbour(),
            bilinear(),
            bicubic()
    });

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