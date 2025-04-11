package com.thedeathlycow.novoatlas.mixin.accessor;

import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import com.thedeathlycow.novoatlas.world.gen.NovoAtlasSurfaceSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SurfaceSystem.class)
public class NovoAtlasSurfaceSystemImpl implements NovoAtlasSurfaceSystem {
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
            MapInfo mapInfo
    ) {
        final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        final ChunkPos chunkPos = chunkAccess.getPos();

        int x = chunkPos.x;
    }
}