package com.thedeathlycow.novoatlas.impl.v2.image.sampler;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.Interpolator;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.ImageTransform;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.Offset;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.Scaling;
import net.minecraft.resources.Identifier;
import org.joml.Vector2fc;
import org.jspecify.annotations.Nullable;

public final class SingleImageSampler implements ImageSampler {
    public static final MapCodec<SingleImageSampler> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    ImageTransform.CODEC
                            .fieldOf("transform")
                            .forGetter(i -> i.transform),
                    Identifier.CODEC
                            .fieldOf("image_id")
                            .forGetter(i -> i.imageId)
            ).apply(instance, SingleImageSampler::new)
    );

    private final ImageTransform transform;
    private final Identifier imageId;
    private @Nullable ImageData data;

    public SingleImageSampler(ImageTransform transform, Identifier imageId) {
        this.transform = transform;
        this.imageId = imageId;
    }

    @Override
    public int getPixelValue(int pixelX, int pixelZ, ImageLoader<? extends ImageData> loader) {
        return this.getImageData(loader).getPixelValue(pixelX, pixelZ);
    }

    @Override
    public MapCodec<SingleImageSampler> codec() {
        return CODEC;
    }

    @Override
    public int width(ImageLoader<? extends ImageData> loader) {
        return this.getImageData(loader).width();
    }

    @Override
    public int height(ImageLoader<? extends ImageData> loader) {
        return this.getImageData(loader).height();
    }

    @Override
    public ImageTransform transform() {
        return this.transform;
    }

    private ImageData getImageData(ImageLoader<? extends ImageData> loader) {
        if (this.data == null) {
            this.data = loader.get(this.imageId).join();
        }

        return this.data;
    }
}