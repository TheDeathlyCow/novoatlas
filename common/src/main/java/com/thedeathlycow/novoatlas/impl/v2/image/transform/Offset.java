package com.thedeathlycow.novoatlas.impl.v2.image.transform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector2f;
import org.joml.Vector2fc;

public record Offset(
        Vector2fc centerOffset,
        int startingY
) {
    public static final Offset DEFAULT = new Offset(new Vector2f(0f, 0f), 0);

    public static final Codec<Offset> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ExtraCodecs.VECTOR2F
                            .optionalFieldOf("center_offset", DEFAULT.centerOffset())
                            .forGetter(Offset::centerOffset),
                    Codec.INT
                            .optionalFieldOf("starting_y", DEFAULT.startingY())
                            .forGetter(Offset::startingY)
            ).apply(instance, Offset::new)
    );
}