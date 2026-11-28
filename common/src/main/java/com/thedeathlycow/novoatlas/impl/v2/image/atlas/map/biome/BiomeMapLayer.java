package com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.BiomeMap;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ColorImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.ImageTransform;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.IntPredicate;

public record BiomeMapLayer(
        Range yRange,
        Holder<BiomeMap> biomes
) {
    public static final Codec<BiomeMapLayer> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Range.CODEC
                            .fieldOf("y_range")
                            .forGetter(BiomeMapLayer::yRange),
                    BiomeMap.CODEC
                            .fieldOf("biomes")
                            .forGetter(BiomeMapLayer::biomes)
            ).apply(instance, BiomeMapLayer::new)
    );

    public boolean isBlockInLayer(int blockY) {
        return this.isInLayer(blockY);
    }

    public boolean isInLayer(int quartY) {
        return this.isInLayer(QuartPos.toBlock(quartY));
    }

    @NonNull
    public Holder<Biome> getBiome(
            int quartX,
            int quartZ,
            ImageLoader<ColorImageData> loader,
            ImageTransform baseTransform,
            Holder<Biome> defaultBiome
    ) {
        return this.biomes.value().getBiome(quartX, quartZ, loader, baseTransform, defaultBiome);
    }

    private record Range(Optional<Integer> min, Optional<Integer> max) implements IntPredicate {
        public static final Codec<Range> CODEC = RecordCodecBuilder.<Range>create(
                instance -> instance.group(
                        Codec.INT
                                .optionalFieldOf("min")
                                .forGetter(Range::min),
                        Codec.INT
                                .optionalFieldOf("max")
                                .forGetter(Range::max)
                ).apply(instance, Range::new)
        ).validate(range -> {
            int minValue = range.min.orElse(Integer.MIN_VALUE);
            int maxValue = range.max.orElse(Integer.MAX_VALUE);

            if (minValue >= maxValue) {
                return DataResult.error(() -> "Min is not less than max in: " + range, range);
            } else {
                return DataResult.success(range);
            }
        });

        @Override
        public boolean test(int y) {
            int minValue = this.min.orElse(Integer.MIN_VALUE);
            int maxValue = this.max.orElse(Integer.MAX_VALUE);

            return minValue <= y && y <= maxValue;
        }
    }
}