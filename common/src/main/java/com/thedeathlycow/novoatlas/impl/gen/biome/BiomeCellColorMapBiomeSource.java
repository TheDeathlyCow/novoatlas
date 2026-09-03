package com.thedeathlycow.novoatlas.impl.gen.biome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.gen.MapInfo;
import com.thedeathlycow.novoatlas.impl.gen.biome.provider.LayeredMapBiomeProvider;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

public class BiomeCellColorMapBiomeSource extends BiomeSource {
    public static final MapCodec<BiomeCellColorMapBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(BiomeCellColorMapBiomeSource::getMapInfo),
                    Biome.CODEC
                            .fieldOf("default_biome")
                            .forGetter(BiomeCellColorMapBiomeSource::getDefaultBiome)
            ).apply(instance, BiomeCellColorMapBiomeSource::new)
    );

    private final Holder<MapInfo> mapInfo;
    private final Holder<Biome> defaultBiome;

    public BiomeCellColorMapBiomeSource(Holder<MapInfo> mapInfo, Holder<Biome> defaultBiome) {
        this.mapInfo = mapInfo;
        this.defaultBiome = defaultBiome;
    }

    @Override
    protected MapCodec<? extends BiomeCellColorMapBiomeSource> codec() {
        return CODEC;
    }

    @Override
    @NotNull
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        MapInfo mapInfoValue = this.mapInfo.value();

        Stream<Holder<Biome>> baseBiomes = mapInfoValue
                .surfaceBiomes()
                .collectPossibleBiomes();

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
    public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler sampler) {
        MapInfo info = this.mapInfo.value();

        return info.getBiome(biomeX, biomeY, biomeZ, this.defaultBiome);
    }

    public Holder<MapInfo> getMapInfo() {
        return mapInfo;
    }

    public Holder<Biome> getDefaultBiome() {
        return defaultBiome;
    }
}