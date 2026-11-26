package com.thedeathlycow.novoatlas.impl.v2.image.interpolation;

import com.thedeathlycow.novoatlas.impl.v2.image.data.ImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import com.thedeathlycow.novoatlas.impl.v2.image.sampler.ImageSampler;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.ImageTransform;
import net.minecraft.util.Mth;

record ContextImpl(ImageSampler sampler, ImageLoader<? extends ImageData> loader) implements Interpolator.Context {
    @Override
    public ImageTransform transform() {
        return sampler.transform();
    }

    @Override
    public int getTruncated(double pixelXR, double pixelZR) {
        return this.getWrappedPixelValue(Mth.floor(pixelXR), Mth.floor(pixelZR));
    }

    @Override
    public int getWrappedPixelValue(int pixelX, int pixelZ) {
        return this.transform().imageWrapping().transform(
                pixelX, pixelZ,
                this.sampler.width(loader), this.sampler.height(loader),
                (x1, z1) -> this.sampler.getPixelValue(x1, z1, loader)
        );
    }
}