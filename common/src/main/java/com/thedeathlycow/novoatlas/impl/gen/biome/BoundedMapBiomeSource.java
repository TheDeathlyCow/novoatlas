package com.thedeathlycow.novoatlas.impl.gen.biome;


import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import com.thedeathlycow.novoatlas.impl.image.biome.provider.LayeredMapBiomeProvider;
import com.thedeathlycow.novoatlas.mixin.accessor.BiomeSourceAccessor;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.stream.Stream;

@ApiStatus.Experimental
public class BoundedMapBiomeSource extends BiomeSource {
    public static final MapCodec<BoundedMapBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(BoundedMapBiomeSource::getMapInfo),
                    Biome.CODEC
                            .fieldOf("default_biome")
                            .forGetter(BoundedMapBiomeSource::getDefaultBiome),
                    BiomeSource.CODEC
                            .fieldOf("outside_map")
                            .forGetter(BoundedMapBiomeSource::getOutsideMap)
            ).apply(instance, BoundedMapBiomeSource::new)
    );

    private final Holder<MapInfo> mapInfo;
    private final Holder<Biome> defaultBiome;
    private final BiomeSource outsideMap;
    private final BiomeResolver insideMapResolver;

    public BoundedMapBiomeSource(Holder<MapInfo> mapInfo, Holder<Biome> defaultBiome, BiomeSource outsideMap) {
        this.mapInfo = mapInfo;
        this.defaultBiome = defaultBiome;
        this.outsideMap = outsideMap;
        this.insideMapResolver = mapInfo.value().createBiomeResolver(defaultBiome);
    }

    @Override
    protected MapCodec<BoundedMapBiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        MapInfo mapInfoValue = this.mapInfo.value();

        Stream<Holder<Biome>> baseBiomes = Stream.concat(
                // this is necessary because java is dumb
                ((BiomeSourceAccessor) outsideMap).invokeCollectPossibleBiomes(),
                mapInfoValue
                        .surfaceBiomes()
                        .collectPossibleBiomes()
        );

        Optional<LayeredMapBiomeProvider> caveBiomes = mapInfoValue.caveBiomes();

        if (caveBiomes.isPresent()) {
            baseBiomes = Stream.concat(
                    baseBiomes,
                    mapInfoValue.caveBiomes().orElseThrow().collectPossibleBiomes()
            );
        }

        return Stream.concat(Stream.of(this.defaultBiome), baseBiomes);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        MapInfo info = this.mapInfo.value();

        if (info.isPointInsideBiomeMap(quartX, quartZ)) {
            return this.insideMapResolver.getNoiseBiome(quartX, quartY, quartZ, sampler);
        }

        return this.outsideMap.getNoiseBiome(quartX, quartY, quartZ, sampler);
    }

    public Holder<MapInfo> getMapInfo() {
        return mapInfo;
    }

    public Holder<Biome> getDefaultBiome() {
        return defaultBiome;
    }

    public BiomeSource getOutsideMap() {
        return outsideMap;
    }
}