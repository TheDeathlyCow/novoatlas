package com.thedeathlycow.novoatlas.impl.v2.image.sampler;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.Interpolator;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.ImageTransform;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.Offset;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.Scaling;
import com.thedeathlycow.novoatlas.impl.v2.registry.NovoAtlasBuiltinRegistries;
import org.joml.Vector2fc;

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

    /// The transform information of the data.
    ImageTransform transform();

    /// The data's type codec. You should register this to [NovoAtlasBuiltinRegistries#IMAGE_SAMPLER_TYPE]
    MapCodec<? extends ImageSampler> codec();
}