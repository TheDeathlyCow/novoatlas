package com.thedeathlycow.novoatlas.impl.gen.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.gen.density.GetPreliminaryHeightFromMapDensityFunction;
import com.thedeathlycow.novoatlas.impl.gen.density.HeightmapDensityFunction;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import org.jspecify.annotations.NonNull;

public final class ImageMapChunkGenerator extends ImageBasedChunkGenerator {
    public static final MapCodec<ImageMapChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BiomeSource.CODEC
                                    .fieldOf("biome_source")
                                    .forGetter(ImageMapChunkGenerator::getBiomeSource),
                            NoiseGeneratorSettings.CODEC
                                    .fieldOf("settings")
                                    .forGetter(ImageMapChunkGenerator::generatorSettings),
                            MapInfo.CODEC
                                    .fieldOf("map_info")
                                    .forGetter(ImageMapChunkGenerator::getMapInfo),
                            DensityFunction.CODEC
                                    .fieldOf("underground_density_function")
                                    .forGetter(ImageMapChunkGenerator::getUndergroundDensityFunction),
                            Codec.BOOL
                                    .optionalFieldOf("enable_carvers", true)
                                    .forGetter(ImageMapChunkGenerator::isEnableCarvers)
                    )
                    .apply(instance, ImageMapChunkGenerator::new)
    );

    public ImageMapChunkGenerator(
            final BiomeSource biomeSource,
            final Holder<NoiseGeneratorSettings> settings,
            final Holder<MapInfo> mapInfo,
            final DensityFunction undergroundDensityFunction,
            final boolean enableCarvers
    ) {
        super(
                biomeSource,
                applyHeightMapToDensityFunctions(settings.value(), mapInfo, undergroundDensityFunction),
                mapInfo,
                undergroundDensityFunction,
                enableCarvers
        );
    }

    private static Holder<NoiseGeneratorSettings> applyHeightMapToDensityFunctions(
            final NoiseGeneratorSettings baseSettings,
            final Holder<MapInfo> mapInfo,
            final DensityFunction undergroundDensityFunction
    ) {
        final NoiseRouter baseNoiseRouter = baseSettings.noiseRouter();
        final NoiseSettings noiseSettings = baseSettings.noiseSettings();

        DensityFunction chunkSurfaceLevel = createPatchedPreliminaryDensity(mapInfo, noiseSettings);
        DensityFunction finalDensity = createPatchedFinalDensity(mapInfo, undergroundDensityFunction);

        return applyPatchedDensityFunctionsToNoiseSettings(baseSettings, baseNoiseRouter, chunkSurfaceLevel, finalDensity);
    }

    @Override
    @NonNull
    protected MapCodec<ImageMapChunkGenerator> codec() {
        return CODEC;
    }
}