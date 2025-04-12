package com.thedeathlycow.novoatlas.world.gen.biome;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class NovoAtlasBiomeSource extends BiomeSource {
    private final Holder<MapInfo> mapInfo;

    private final List<BiomeColorEntry> biomeColors;

    private final Holder<Biome> defaultBiome;

    private final Optional<Climate.ParameterList<Holder<Biome>>> caveBiomes;

    private final int caveBiomeDepth;

    private final Int2ObjectArrayMap<Holder<Biome>> biomeToColorCache = new Int2ObjectArrayMap<>();

    public NovoAtlasBiomeSource(
            Holder<MapInfo> mapInfo,
            List<BiomeColorEntry> biomeColors,
            Holder<Biome> defaultBiome,
            Optional<Climate.ParameterList<Holder<Biome>>> caveBiomes,
            int caveBiomeDepth
    ) {
        this.mapInfo = mapInfo;
        this.biomeColors = biomeColors;
        this.defaultBiome = defaultBiome;
        this.caveBiomes = caveBiomes;
        this.caveBiomeDepth = caveBiomeDepth;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return null;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return null;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
        return null;
    }
}