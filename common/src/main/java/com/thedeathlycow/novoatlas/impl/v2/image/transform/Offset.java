package com.thedeathlycow.novoatlas.impl.v2.image.transform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Offset(
        int absoluteY,
        double relativeX,
        double relativeZ
) {
    public static final Offset DEFAULT = new Offset(0, 0.0, 0.0);

    public static final Codec<Offset> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT
                            .optionalFieldOf("absolute_y", DEFAULT.absoluteY())
                            .forGetter(Offset::absoluteY),
                    Codec.DOUBLE
                            .optionalFieldOf("relative_x", DEFAULT.relativeX())
                            .forGetter(Offset::relativeX),
                    Codec.DOUBLE
                            .optionalFieldOf("relative_z", DEFAULT.relativeZ())
                            .forGetter(Offset::relativeZ)
            ).apply(instance, Offset::new)
    );

    public boolean isUnitOffset() {
        return this == DEFAULT || (this.absoluteY == 0 && this.relativeX == 0.0 && this.relativeZ == 0.0);
    }

    public Offset merge(Offset other) {
        if (this.isUnitOffset()) {
            return other;
        }

        if (other.isUnitOffset()) {
            return this;
        }

        return new Offset(
                this.absoluteY + other.absoluteY,
                this.relativeX + other.relativeX,
                this.relativeZ + other.relativeZ
        );
    }
}