package com.thedeathlycow.novoatlas.impl.v2.image.io;

import com.thedeathlycow.novoatlas.impl.v2.image.data.ColorImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.data.GrayscaleImageData;
import org.apache.commons.lang3.NotImplementedException;

public interface NovoAtlasMinecraftServer {
    default ImageLoader<GrayscaleImageData> novoatlas$grayscaleLoader() {
        throw new NotImplementedException("Implemented in mixin");
    }

    default ImageLoader<ColorImageData> novoatlas$colorLoader() {
        throw new NotImplementedException("Implemented in mixin");
    }
}
