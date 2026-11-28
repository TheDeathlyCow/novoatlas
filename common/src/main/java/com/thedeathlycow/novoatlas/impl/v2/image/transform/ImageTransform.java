package com.thedeathlycow.novoatlas.impl.v2.image.transform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.Interpolator;

import java.util.Optional;

public record ImageTransform(
        Optional<ImageWrapping> imageWrapping,
        Scaling scaling,
        Offset offset
) {
    public static final ImageTransform DEFAULT = new ImageTransform(
            Optional.empty(),
            Scaling.DEFAULT,
            Offset.DEFAULT
    );

    public static final Codec<ImageTransform> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ImageWrapping.CODEC
                            .optionalFieldOf("image_wrapping")
                            .forGetter(ImageTransform::imageWrapping),
                    Scaling.CODEC
                            .optionalFieldOf("scaling", DEFAULT.scaling())
                            .forGetter(ImageTransform::scaling),
                    Offset.CODEC
                            .optionalFieldOf("offset", DEFAULT.offset())
                            .forGetter(ImageTransform::offset)
            ).apply(instance, ImageTransform::new)
    );

    public boolean isUnitTransform() {
        return this == DEFAULT || (this.imageWrapping.isEmpty() && this.scaling.isUnitScaling() && this.offset.isUnitOffset());
    }

    /// Merges another transform into this one.
    ///
    /// The scales are multiplied, and the offsets are added.
    ///
    /// Uses this transform's wrapping and/or interpolation if they are non-empty, otherwise falls back to the wrapping
    /// and/or interpolation provided by `other`.
    public ImageTransform merge(ImageTransform other) {
        if (this.isUnitTransform()) {
            return other;
        }

        if (other.isUnitTransform()) {
            return this;
        }

        return new ImageTransform(
                this.imageWrapping.or(() -> other.imageWrapping),
                this.scaling.merge(other.scaling),
                this.offset.merge(other.offset)
        );
    }

    public ImageWrapping getImageWrapping() {
        return this.imageWrapping.orElse(ImageWrapping.CLAMP_TO_EDGE);
    }

    public float getVerticalScaleValue() {
        return this.scaling.verticalScale();
    }

    public float getHorizontalScaleValue() {
        return this.scaling.horizontalScale().value();
    }

    public Interpolator getHorizontalScaleInterpolation() {
        return this.scaling.horizontalScale().getInterpolation();
    }

    public double getRelativeOffsetX() {
        return this.offset.relativeX();
    }

    public double getRelativeOffsetZ() {
        return this.offset.relativeZ();
    }

    public int getStartingY() {
        return this.offset.absoluteY();
    }
}