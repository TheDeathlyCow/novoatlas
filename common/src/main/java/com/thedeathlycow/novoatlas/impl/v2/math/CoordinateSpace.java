package com.thedeathlycow.novoatlas.impl.v2.math;

import com.thedeathlycow.novoatlas.impl.v2.image.transform.ImageTransform;

public final class CoordinateSpace {
    public static double coordToIndexX(int coordIndex, ImageTransform transform, double size) {
        return CoordinateSpace.coordToIndexWithAlpha(
                coordIndex,
                transform.scaling().horizontalScale().value(),
                transform.offset().relativeX(),
                size
        );
    }

    public static double coordToIndexZ(int coordIndex, ImageTransform transform, double size) {
        return CoordinateSpace.coordToIndexWithAlpha(
                coordIndex,
                transform.scaling().horizontalScale().value(),
                transform.offset().relativeZ(),
                size
        );
    }

    public static double coordToIndexWithAlpha(int coordIndex, double horizontalScale, double centerOffset, double size) {
        double iR = coordIndex - (centerOffset * size);
        return (iR / horizontalScale) + size * 0.5;
    }

    public static double indexToCoord(int index, double horizontalScale, double centerOffset, double size) {
        return horizontalScale * (index - (size * 0.5)) + centerOffset * size;
    }

    private CoordinateSpace() {

    }
}