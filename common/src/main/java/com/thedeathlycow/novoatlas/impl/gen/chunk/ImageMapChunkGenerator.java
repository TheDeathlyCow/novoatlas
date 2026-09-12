package com.thedeathlycow.novoatlas.impl.gen.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.gen.density.GetPreliminaryHeightFromMapDensityFunction;
import com.thedeathlycow.novoatlas.impl.gen.density.HeightmapDensityFunction;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.*;
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
            BiomeSource biomeSource,
            Holder<NoiseGeneratorSettings> settings,
            Holder<MapInfo> mapInfo,
            DensityFunction undergroundDensityFunction,
            boolean enableCarvers
    ) {
        super(
                biomeSource,
                applyHeightMapToDensityFunctions(settings, mapInfo, undergroundDensityFunction),
                mapInfo,
                undergroundDensityFunction,
                enableCarvers
        );
    }

    private static Holder<NoiseGeneratorSettings> applyHeightMapToDensityFunctions(
            Holder<NoiseGeneratorSettings> settings,
            Holder<MapInfo> mapInfo,
            DensityFunction undergroundDensityFunction
    ) {
        final NoiseGeneratorSettings baseSettings = settings.value();
        final NoiseRouter baseNoiseRouter = baseSettings.noiseRouter();
        final NoiseSettings noiseSettings = baseSettings.noiseSettings();

        final int minY = noiseSettings.minY();
        final int maxY = minY + noiseSettings.height();

        DensityFunction preliminaryHeightmap = new GetPreliminaryHeightFromMapDensityFunction(mapInfo, minY, maxY);

        DensityFunction finalDensity = DensityFunctions.min(
                new HeightmapDensityFunction(mapInfo, 128.0),
                undergroundDensityFunction
        );

        NoiseRouter fixedNoiseRouter = new NoiseRouter(
                baseNoiseRouter.barrierNoise(),
                baseNoiseRouter.fluidLevelFloodednessNoise(),
                baseNoiseRouter.fluidLevelSpreadNoise(),
                baseNoiseRouter.lavaNoise(),
                baseNoiseRouter.temperature(),
                baseNoiseRouter.vegetation(),
                baseNoiseRouter.continents(),
                baseNoiseRouter.erosion(),
                baseNoiseRouter.depth(),
                baseNoiseRouter.ridges(),
                preliminaryHeightmap,
                finalDensity,
                baseNoiseRouter.veinToggle(),
                baseNoiseRouter.veinRidged(),
                baseNoiseRouter.veinGap()
        );

        NoiseGeneratorSettings fixedSettings = new NoiseGeneratorSettings(
                baseSettings.noiseSettings(),
                baseSettings.defaultBlock(),
                baseSettings.defaultFluid(),
                fixedNoiseRouter,
                baseSettings.surfaceRule(),
                baseSettings.spawnTarget(),
                baseSettings.seaLevel(),
                baseSettings.disableMobGeneration(),
                baseSettings.aquifersEnabled(),
                baseSettings.oreVeinsEnabled(),
                baseSettings.useLegacyRandomSource()
        );

        return Holder.direct(fixedSettings);
    }

    @Override
    @NonNull
    protected MapCodec<ImageMapChunkGenerator> codec() {
        return CODEC;
    }
}