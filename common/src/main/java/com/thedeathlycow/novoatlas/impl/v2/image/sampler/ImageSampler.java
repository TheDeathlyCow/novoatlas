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

    /// Samples a block in some coordinate space and returns the pixel value with full interpolation.
    default int sample(int coordX, int coordZ, ImageLoader<? extends ImageData> loader) {
        Scaling scaling = this.transform().scaling();
        Offset offset = this.transform().offset();

        Scaling.InterpolatedScale horizontalScale = scaling.horizontalScale();
        float horizontalScaleValue = horizontalScale.value();
        Vector2fc centerOffset = offset.centerOffset();

        double xR = coordToIndexWithAlpha(coordX, horizontalScaleValue, centerOffset.x(), this.width(loader));
        double zR = coordToIndexWithAlpha(coordZ, horizontalScaleValue, centerOffset.y(), this.height(loader));

        double height = horizontalScale.interpolation().sample(xR, zR, Interpolator.context(this, loader));

        return (int) Math.round(scaling.verticalScale() * height + offset.startingY());
    }

    /// Gets the pixel value in pixel space. Coordinates are already wrapped for you, no need to apply wrapping.
    int getPixelValue(int pixelX, int pixelZ, ImageLoader<? extends ImageData> loader);

    /// Gets the width of the sampler in pixels.
    int width(ImageLoader<? extends ImageData> loader);

    /// Gets the height of the sampler in pixels.
    int height(ImageLoader<? extends ImageData> loader);

    /// The transform information of the sampler.
    ImageTransform transform();

    /// The sampler's type codec. You should register this to [NovoAtlasBuiltinRegistries#IMAGE_SAMPLER_TYPE]
    MapCodec<? extends ImageSampler> codec();

    private static double coordToIndexWithAlpha(int coord, double horizontalScale, double centerOffset, double size) {
        double iR = coord - (centerOffset * size);
        return (iR / horizontalScale) + size * 0.5;
    }

    private static double indexToCoord(int index, double horizontalScale, double centerOffset, double size) {
        return horizontalScale * (index - (size * 0.5)) + centerOffset * size;
    }
}