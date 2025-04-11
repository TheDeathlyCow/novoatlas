package com.thedeathlycow.novoatlas.world.gen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.NovoAtlas;
import com.thedeathlycow.novoatlas.mixin.accessor.NoiseBasedChunkGeneratorAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jetbrains.annotations.NotNull;

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
    public int getBaseHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return (int) this.getFromMap(x, z, this.mapInfo.value().lookupHeightmap());
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

    private double getFromMap(int x, int z, @NotNull MapImage mapImage) {
        MapInfo info = this.mapInfo.value();

        float xR = (x / info.horizontalScale()) + mapImage.width() / 2f; // these will always be even numbers
        float zR = (z / info.horizontalScale()) + mapImage.height() / 2f;

        if (xR < 0 || zR < 0 || xR >= mapImage.width() || zR >= mapImage.height()) {
            return this.getMinY() - 1;
        }

        int truncatedX = Mth.floor(xR);
        int truncatedZ = Mth.floor(zR);

        double height = mapImage.bilerp(truncatedX, xR - truncatedX, truncatedZ, zR - truncatedZ);

        return info.verticalScale() * height + info.startingY();
    }
}