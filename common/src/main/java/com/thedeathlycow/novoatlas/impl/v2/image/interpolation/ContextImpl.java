package com.thedeathlycow.novoatlas.impl.v2.image.interpolation;

import com.thedeathlycow.novoatlas.impl.v2.image.data.ImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import com.thedeathlycow.novoatlas.impl.v2.image.sampler.ImageSampler;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.ImageTransform;
import net.minecraft.util.Mth;

record ContextImpl(ImageSampler sampler, ImageTransform transform,
                   ImageLoader<? extends ImageData> loader) implements Interpolator.Context {
    @Override
    public ImageTransform transform() {
        return this.transform;
    }

    @Override
    public int getTruncated(double pixelXR, double pixelZR) {
        return this.sampler.getWrappedPixelValue(Mth.floor(pixelXR), Mth.floor(pixelZR), this.transform, this.loader);
    }

    @Override
    public int getWrappedPixelValue(int pixelX, int pixelZ) {
        return this.sampler.getWrappedPixelValue(pixelX, pixelZ, this.transform, this.loader);
    }
}