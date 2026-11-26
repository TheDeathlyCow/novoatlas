package com.thedeathlycow.novoatlas.impl.v2.image.interpolation;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import com.thedeathlycow.novoatlas.impl.v2.image.sampler.ImageSampler;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.ImageTransform;
import com.thedeathlycow.novoatlas.impl.v2.registry.NovoAtlasBuiltinRegistries;

import java.util.function.Function;

/// Interpolation logic
/// TODO: make this public java api
public interface Interpolator {
    MapCodec<Interpolator> BASE_CODEC = NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE.byNameCodec()
            .dispatchMap(
                    "interpolation",
                    Interpolator::codec,
                    Function.identity()
            );

    interface Context {
        ImageSampler sampler();

        ImageTransform transform();

        int getTruncated(double pixelXR, double pixelZR);

        int getWrappedPixelValue(int pixelX, int pixelZ);
    }

    static Context context(ImageSampler sampler, ImageLoader<? extends ImageData> loader) {
        return new ContextImpl(sampler, loader);
    }

    double sample(double pixelX, double pixelZ, Context ctx);

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