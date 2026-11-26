package com.thedeathlycow.novoatlas.impl.v2.image.sampler;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.ImageTransform;
import com.thedeathlycow.novoatlas.impl.v2.registry.NovoAtlasBuiltinRegistries;
import net.minecraft.util.Mth;

import java.util.function.Function;

public interface ImageSampler {
    MapCodec<ImageSampler> BASE_CODEC = NovoAtlasBuiltinRegistries.IMAGE_SAMPLER_TYPE
            .byNameCodec()
            .dispatchMap(ImageSampler::codec, Function.identity());

    /// Gets the pixel value in pixel space. Coordinates are already wrapped for you, no need to apply wrapping.
    int getPixelValue(int pixelX, int pixelZ, ImageLoader<? extends ImageData> loader);

    /// Gets the width of the data in pixels.
    int width(ImageLoader<? extends ImageData> loader);

    /// Gets the height of the data in pixels.
    int height(ImageLoader<? extends ImageData> loader);

    /// The data's type codec. You should register this to [NovoAtlasBuiltinRegistries#IMAGE_SAMPLER_TYPE]
    MapCodec<? extends ImageSampler> codec();

    default int getTruncated(double pixelXR, double pixelZR, ImageTransform transform, ImageLoader<? extends ImageData> loader) {
        return this.getWrappedPixelValue(Mth.floor(pixelXR), Mth.floor(pixelZR), transform, loader);
    }

    default int getWrappedPixelValue(int pixelX, int pixelZ, ImageTransform transform, ImageLoader<? extends ImageData> loader) {
        return transform.imageWrapping().transform(
                pixelX, pixelZ,
                this.width(loader), this.height(loader),
                (x1, z1) -> this.getPixelValue(x1, z1, loader)
        );
    }
}