package com.thedeathlycow.novoatlas.world.gen;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public interface NovoAtlasSurfaceSystem {
    default void novoatlas$buildSurface(
            RandomState randomState,
            BiomeManager biomeManager,
            Registry<Biome> registry,
            boolean useLegacyRandom,
            WorldGenerationContext worldGenerationContext,
            ChunkAccess chunkAccess,
            NoiseChunk noiseChunk,
            SurfaceRules.RuleSource ruleSource,
            Holder<MapInfo> mapInfo
    ) {
        throw new UnsupportedOperationException("Implemented in mixin");
    }
}
