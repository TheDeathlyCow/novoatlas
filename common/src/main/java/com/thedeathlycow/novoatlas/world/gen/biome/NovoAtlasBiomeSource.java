package com.thedeathlycow.novoatlas.world.gen.biome;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.stream.Stream;

public class NovoAtlasBiomeSource extends BiomeSource {
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