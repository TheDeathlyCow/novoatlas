package com.thedeathlycow.novoatlas.impl.v2.image.transform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ImageTransform(
        ImageWrapping imageWrapping,
        Scaling scaling,
        Offset offset
) {
    public static final ImageTransform DEFAULT = new ImageTransform(
            ImageWrapping.CLAMP_TO_EDGE,
            Scaling.DEFAULT,
            Offset.DEFAULT
    );

    public static final Codec<ImageTransform> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ImageWrapping.CODEC
                            .optionalFieldOf("image_wrapping", DEFAULT.imageWrapping())
                            .forGetter(ImageTransform::imageWrapping),
                    Scaling.CODEC
                            .optionalFieldOf("scaling", DEFAULT.scaling())
                            .forGetter(ImageTransform::scaling),
                    Offset.CODEC
                            .optionalFieldOf("offset", DEFAULT.offset())
                            .forGetter(ImageTransform::offset)
            ).apply(instance, ImageTransform::new)
    );
}