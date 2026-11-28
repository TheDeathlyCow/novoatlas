package com.thedeathlycow.novoatlas.impl.v2.image.data;

/// Stores raw image data, does not apply any transformations.
/// Images are loaded by [com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader] and can be accessed on any instance
/// of [net.minecraft.server.MinecraftServer] via [com.thedeathlycow.novoatlas.impl.v2.image.io.NovoAtlasMinecraftServer].
public interface ImageData {
    int width();

    int height();

    int getPixelValue(int x, int z);
}
