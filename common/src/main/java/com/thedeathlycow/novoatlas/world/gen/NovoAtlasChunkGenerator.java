package com.thedeathlycow.novoatlas.world.gen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.NovoAtlas;
import com.thedeathlycow.novoatlas.mixin.accessor.NoiseBasedChunkGeneratorAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;

public class NovoAtlasChunkGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<NovoAtlasChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BiomeSource.CODEC
                                    .fieldOf("biome_source")
                                    .forGetter(NovoAtlasChunkGenerator::getBiomeSource),
                            NoiseGeneratorSettings.CODEC
                                    .fieldOf("settings")
                                    .forGetter(NovoAtlasChunkGenerator::generatorSettings),
                            MapInfo.CODEC
                                    .fieldOf("map_info")
                                    .forGetter(NovoAtlasChunkGenerator::getMapInfo)
                    )
                    .apply(instance, NovoAtlasChunkGenerator::new)
    );

    private final Holder<MapInfo> mapInfo;

    public NovoAtlasChunkGenerator(
            BiomeSource biomeSource,
            Holder<NoiseGeneratorSettings> settings,
            Holder<MapInfo> mapInfo
    ) {
        super(biomeSource, settings);
        this.mapInfo = mapInfo;

        if (this.mapInfo.value().verticalScale() != 1f) {
            NovoAtlas.LOGGER.warn("Using non-standard vertical scale, expect weird generation!");
        }
    }

    @Override
    protected MapCodec<? extends NovoAtlasChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void buildSurface(
            ChunkAccess chunkAccess,
            WorldGenerationContext worldGenerationContext,
            RandomState randomState,
            StructureManager structureManager,
            BiomeManager biomeManager,
            Registry<Biome> registry,
            Blender blender
    ) {
        NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(chunk -> {
            return ((NoiseBasedChunkGeneratorAccessor) this).invokeCreateNoiseChunk(
                    chunk,
                    structureManager,
                    blender,
                    randomState
            );
        });

        NoiseGeneratorSettings noiseGeneratorSettings = this.generatorSettings().value();
        ((NovoAtlasSurfaceSystem) randomState.surfaceSystem())
                .novoatlas$buildSurface(
                        randomState,
                        biomeManager,
                        registry,
                        noiseGeneratorSettings.useLegacyRandomSource(),
                        worldGenerationContext,
                        chunkAccess,
                        noiseChunk,
                        noiseGeneratorSettings.surfaceRule(),
                        this.mapInfo
                );
    }

    public Holder<MapInfo> getMapInfo() {
        return mapInfo;
    }
}