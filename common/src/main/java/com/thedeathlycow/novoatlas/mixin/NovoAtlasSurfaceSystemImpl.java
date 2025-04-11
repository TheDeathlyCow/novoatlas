package com.thedeathlycow.novoatlas.mixin;

import com.llamalad7.mixinextras.sugar.Local;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(
            method = "buildSurface",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/SurfaceRules$RuleSource;apply(Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private void addMapInfoToContext(
            RandomState randomState,
            BiomeManager biomeManager,
            Registry<Biome> registry,
            boolean useLegacyRandom,
            WorldGenerationContext worldGenerationContext,
            ChunkAccess chunkAccess,
            NoiseChunk noiseChunk,
            SurfaceRules.RuleSource ruleSource,
            CallbackInfo ci,
            @Local(ordinal = 0) SurfaceRules.Context context
    ) {
    }
}