package com.thedeathlycow.novoatlas.impl.v1.image;


import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import com.thedeathlycow.novoatlas.impl.v2.image.sampler.ImageSampler;

@Deprecated
record LegacyImageSampler(ImageData image) implements ImageSampler {
    LegacyImageSampler(MapImage image) {
        this(new LegacyMapImage(image));
    }

    @Override
    public int getPixelValue(int pixelX, int pixelZ, ImageLoader<? extends ImageData> loader) {
        return this.image.getPixelValue(pixelX, pixelZ);
    }

    @Override
    public int width(ImageLoader<? extends ImageData> loader) {
        return this.image.width();
    }

    @Override
    public int height(ImageLoader<? extends ImageData> loader) {
        return this.image.height();
    }

    @Override
    public MapCodec<? extends ImageSampler> codec() {
        return null;
    }
}