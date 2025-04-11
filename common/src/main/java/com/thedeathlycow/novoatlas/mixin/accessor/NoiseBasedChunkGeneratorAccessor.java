package com.thedeathlycow.novoatlas.mixin.accessor;

import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NoiseBasedChunkGenerator.class)
public interface NoiseBasedChunkGeneratorAccessor {
    @Invoker("createNoiseChunk")
    NoiseChunk invokeCreateNoiseChunk(
            ChunkAccess chunkAccess,
            StructureManager structureManager,
            Blender blender,
            RandomState randomState
    );
}
