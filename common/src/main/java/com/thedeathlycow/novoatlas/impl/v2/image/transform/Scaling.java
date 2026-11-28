package com.thedeathlycow.novoatlas.impl.v2.image.transform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.Interpolator;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;

public record Scaling(
        float verticalScale,
        InterpolatedScale horizontalScale
) {
    public static final Scaling DEFAULT = new Scaling(1f, Scaling.InterpolatedScale.DEFAULT);

    public static final Codec<Scaling> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ExtraCodecs.POSITIVE_FLOAT
                            .optionalFieldOf("vertical_scale", 1.0f)
                            .forGetter(Scaling::verticalScale),
                    InterpolatedScale.CODEC
                            .optionalFieldOf("horizontal_scale", InterpolatedScale.DEFAULT)
                            .forGetter(Scaling::horizontalScale)
            ).apply(instance, Scaling::new)
    );

    public boolean isUnitScaling() {
        return this == DEFAULT || (this.verticalScale == 1.0f && this.horizontalScale.value == 1.0f && this.horizontalScale.interpolation.isEmpty());
    }

    public Scaling merge(Scaling other) {
        if (other.isUnitScaling()) {
            return this;
        }

        return new Scaling(
                this.verticalScale * other.verticalScale,
                new InterpolatedScale(
                        this.horizontalScale.interpolation.or(() -> other.horizontalScale.interpolation),
                        this.horizontalScale.value * other.horizontalScale.value
                )
        );
    }

    public record InterpolatedScale(Optional<Interpolator> interpolation, float value) {
        public static final InterpolatedScale DEFAULT = new InterpolatedScale(Optional.empty(), 1.0f);

        private static final Codec<InterpolatedScale> BASE_CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Interpolator.BASE_CODEC.codec()
                                .optionalFieldOf("interpolation")
                                .forGetter(InterpolatedScale::interpolation),
                        ExtraCodecs.POSITIVE_FLOAT
                                .fieldOf("value")
                                .forGetter(InterpolatedScale::value)
                ).apply(instance, InterpolatedScale::new)
        );

        private static final Codec<InterpolatedScale> FLOAT_CODEC = ExtraCodecs.POSITIVE_FLOAT
                .flatComapMap(
                        InterpolatedScale::new,
                        config -> {
                            if (config.interpolation == DEFAULT.interpolation()) {
                                return DataResult.success(config.value);
                            } else {
                                return DataResult.error(() -> "Only a nearest neighbour interpolator can be mapped to a float, but was given:" + config);
                            }
                        }
                );

        public static final Codec<InterpolatedScale> CODEC = Codec.withAlternative(BASE_CODEC, FLOAT_CODEC);

        public InterpolatedScale(float value) {
            this(DEFAULT.interpolation(), value);
        }

        public Interpolator getInterpolation() {
            return this.interpolation().orElse(Interpolator.nearestNeighbour());
        }
    }
}