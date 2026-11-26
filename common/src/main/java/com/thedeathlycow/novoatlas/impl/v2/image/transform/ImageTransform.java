package com.thedeathlycow.novoatlas.impl.v2.image.transform;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ImageTransform(
        ImageWrapping imageWrapping,
        Scaling scaling,
        Offset offset
) {
    public static final MapCodec<ImageTransform> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    ImageWrapping.CODEC
                            .optionalFieldOf("image_wrapping", ImageWrapping.CLAMP_TO_EDGE)
                            .forGetter(ImageTransform::imageWrapping),
                    Scaling.CODEC.codec()
                            .optionalFieldOf("scaling", Scaling.DEFAULT)
                            .forGetter(ImageTransform::scaling),
                    Offset.CODEC.codec()
                            .optionalFieldOf("offset", Offset.DEFAULT)
                            .forGetter(ImageTransform::offset)
            ).apply(instance, ImageTransform::new)
    );
}