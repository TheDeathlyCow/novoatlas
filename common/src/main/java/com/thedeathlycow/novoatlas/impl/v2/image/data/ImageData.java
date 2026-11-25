package com.thedeathlycow.novoatlas.impl.v2.image.data;

import java.awt.image.BufferedImage;

public sealed interface ImageData permits ColorImageData, GrayscaleImageData {
    int getPixelValue(int x, int z);
}
