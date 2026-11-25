package com.thedeathlycow.novoatlas.impl.v2.image.data;

import com.thedeathlycow.novoatlas.impl.v1.image.MapImage;

/// Wraps the old [MapImage] with new API.
///
/// @deprecated Should be considered deprecated along with the rest of the v1 API.
@Deprecated
public record LegacyMapImage(MapImage legacyImage) implements ImageData {
    @Override
    public int width() {
        return this.legacyImage.width();
    }

    @Override
    public int height() {
        return this.legacyImage.height();
    }

    @Override
    public int getPixelValue(int x, int z) {
        return this.legacyImage.getPixelValue(x, z);
    }
}