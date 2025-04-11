package com.thedeathlycow.novoatlas.mixin.accessor;

import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import com.thedeathlycow.novoatlas.world.gen.NovoAtlasSurfaceSystem;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SurfaceSystem.class)
public abstract class NovoAtlasSurfaceSystemImpl implements NovoAtlasSurfaceSystem {
    @Shadow
    public abstract void buildSurface(
            RandomState randomState,
            BiomeManager biomeManager,
            Registry<Biome> registry,
            boolean bl,
            WorldGenerationContext worldGenerationContext,
            ChunkAccess chunkAccess,
            NoiseChunk noiseChunk,
            SurfaceRules.RuleSource ruleSource
    );

    @Unique
    private ThreadLocal<MapInfo> novoatlas$mapInfo = new ThreadLocal<>();

    @Override
    public void novoatlas$buildSurface(
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
        this.novoatlas$mapInfo.set(mapInfo.value());
        this.buildSurface(randomState, biomeManager, registry, useLegacyRandom, worldGenerationContext, chunkAccess, noiseChunk, ruleSource);
    }
}