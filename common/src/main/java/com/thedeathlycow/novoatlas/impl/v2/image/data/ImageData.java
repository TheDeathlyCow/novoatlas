package com.thedeathlycow.novoatlas.impl.v2.image.data;

import com.thedeathlycow.novoatlas.impl.v2.image.ImageWrapping;
import net.minecraft.util.Mth;

/// Stores raw image data, does not apply any transformations.
/// Images are loaded by [com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader] and can be accessed on any instance
/// of [net.minecraft.server.MinecraftServer] via [com.thedeathlycow.novoatlas.impl.v2.image.io.NovoAtlasMinecraftServer].
public interface ImageData {
    int width();

    int height();

    int getPixelValue(int x, int z);

    default int getTruncated(double x, double z, ImageWrapping wrapping) {
        return this.getWrappedPixelValue(Mth.floor(x), Mth.floor(z), wrapping);
    }

    default int getWrappedPixelValue(int x, int z, ImageWrapping wrapping) {
        return wrapping.transform(x, z, this.width(), this.height(), this::getPixelValue);
    }
}
