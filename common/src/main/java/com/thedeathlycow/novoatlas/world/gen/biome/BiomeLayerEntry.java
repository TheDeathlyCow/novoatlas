package com.thedeathlycow.novoatlas.world.gen.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.biome.BiomeSource;

import java.util.List;
import java.util.Optional;
import java.util.function.IntPredicate;

public record BiomeLayerEntry(
        Range offsetRange,
        BiomeSource biomeSource
) {
    public static final Codec<BiomeLayerEntry> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Range.CODEC
                            .fieldOf("offset_range")
                            .forGetter(BiomeLayerEntry::offsetRange),
                    BiomeSource.CODEC
                            .fieldOf("biome_source")
                            .forGetter(BiomeLayerEntry::biomeSource)
            ).apply(instance, BiomeLayerEntry::new)
    );

    public boolean isInLayer(int offset) {
        return offsetRange.test(offset);
    }

    private record Range(Optional<Integer> min, Optional<Integer> max) implements IntPredicate {
        public static final Codec<Range> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.INT
                                .optionalFieldOf("min")
                                .forGetter(Range::min),
                        Codec.INT
                                .optionalFieldOf("max")
                                .forGetter(Range::max)
                ).apply(instance, Range::new)
        );

        @Override
        public boolean test(int number) {
            int minValue = min.orElse(Integer.MIN_VALUE);
            int maxValue = min.orElse(Integer.MAX_VALUE);

            return minValue <= number && number <= maxValue;
        }
    }
}