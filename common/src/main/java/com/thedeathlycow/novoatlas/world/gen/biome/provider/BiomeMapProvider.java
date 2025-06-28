package com.thedeathlycow.novoatlas.world.gen.biome.provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.registry.NovoAtlasRegistries;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.stream.Stream;

public interface BiomeMapProvider {
    Codec<BiomeMapProvider> CODEC = NovoAtlasRegistries.BIOME_MAP_PROVIDER
            .byNameCodec()
            .dispatch(BiomeMapProvider::getCodec, Function.identity());

    @Nullable
    Holder<Biome> getBiome(int x, int y, int z, MapInfo info);

    Stream<Holder<Biome>> collectPossibleBiomes();

    MapCodec<? extends BiomeMapProvider> getCodec();
}