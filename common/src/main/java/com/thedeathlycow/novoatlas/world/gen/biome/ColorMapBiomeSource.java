package com.thedeathlycow.novoatlas.world.gen.biome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ColorMapBiomeSource extends BiomeSource {
    public static final MapCodec<ColorMapBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(ColorMapBiomeSource::getMapInfo),
                    Biome.CODEC
                            .fieldOf("default_biome")
                            .forGetter(ColorMapBiomeSource::getDefaultBiome)
            ).apply(instance, ColorMapBiomeSource::new)
    );

    private final Holder<MapInfo> mapInfo;
    private final Holder<Biome> defaultBiome;

    public ColorMapBiomeSource(Holder<MapInfo> mapInfo, Holder<Biome> defaultBiome) {
        this.mapInfo = mapInfo;
        this.defaultBiome = defaultBiome;
    }

    @Override
    protected MapCodec<? extends ColorMapBiomeSource> codec() {
        return CODEC;
    }

    @Override
    @NotNull
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        Stream<Holder<Biome>> baseBiomes = Stream.concat(
                mapInfo.value().caveBiomes().collectPossibleBiomes(),
                mapInfo.value().surfaceBiomes().collectPossibleBiomes()
        );

        return Stream.concat(Stream.of(defaultBiome), baseBiomes);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler sampler) {
        MapInfo info = this.mapInfo.value();
        int x = QuartPos.toBlock(biomeX);
        int y = QuartPos.toBlock(biomeY);
        int z = QuartPos.toBlock(biomeZ);

        return info.getBiome(x, y, z, this.defaultBiome);
    }

    public Holder<MapInfo> getMapInfo() {
        return mapInfo;
    }

    public Holder<Biome> getDefaultBiome() {
        return defaultBiome;
    }
}