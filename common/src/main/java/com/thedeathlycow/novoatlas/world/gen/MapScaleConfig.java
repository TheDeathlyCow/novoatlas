package com.thedeathlycow.novoatlas.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record MapScaleConfig(
        float verticalScale,
        HorizontalConfig horizontalScale
) {
    public static final Codec<MapScaleConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ExtraCodecs.POSITIVE_FLOAT
                            .optionalFieldOf("vertical_scale", 1.0f)
                            .forGetter(MapScaleConfig::verticalScale),
                    HorizontalConfig.CODEC
                            .optionalFieldOf("horizontal_scale", HorizontalConfig.DEFAULT)
                            .forGetter(MapScaleConfig::horizontalScale)
            ).apply(instance, MapScaleConfig::new)
    );

    public record HorizontalConfig(
            InterpolationStrategy interpolation,
            float value
    ) {
        public static final HorizontalConfig DEFAULT = new HorizontalConfig(InterpolationStrategy.BILINEAR, 1.0f);

        public HorizontalConfig(float value) {
            this(InterpolationStrategy.BILINEAR, value);
        }

        private static final Codec<HorizontalConfig> BASE_CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        InterpolationStrategy.CODEC
                                .fieldOf("interpolation")
                                .forGetter(HorizontalConfig::interpolation),
                        ExtraCodecs.POSITIVE_FLOAT
                                .fieldOf("value")
                                .forGetter(HorizontalConfig::value)
                ).apply(instance, HorizontalConfig::new)
        );

        private static final Codec<HorizontalConfig> FLOAT_CODEC = ExtraCodecs.POSITIVE_FLOAT
                .flatComapMap(
                        HorizontalConfig::new,
                        config -> {
                            if (config.interpolation == DEFAULT.interpolation()) {
                                return DataResult.success(config.value);
                            } else {
                                return DataResult.error(() -> "Non-bilinear horizontal config cannot map to float " + config);
                            }
                        }
                );

        public static final Codec<HorizontalConfig> CODEC = Codec.withAlternative(BASE_CODEC, FLOAT_CODEC);

        public double interpolate(double x, double z, MapImage image) {
            return this.interpolation.interpolate(x, z, image);
        }
    }
}