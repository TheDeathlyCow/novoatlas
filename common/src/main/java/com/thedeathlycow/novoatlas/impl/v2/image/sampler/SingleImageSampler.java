package com.thedeathlycow.novoatlas.impl.v2.image.sampler;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class SingleImageSampler implements ImageSampler {
    public static final MapCodec<SingleImageSampler> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Identifier.CODEC
                            .fieldOf("image_id")
                            .forGetter(i -> i.imageId)
            ).apply(instance, SingleImageSampler::new)
    );

    private final Identifier imageId;
    private @Nullable ImageData data;

    public SingleImageSampler(Identifier imageId) {
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

    private ImageData getImageData(ImageLoader<? extends ImageData> loader) {
        if (this.data == null) {
            this.data = loader.get(this.imageId).join();
        }

        return this.data;
    }
}