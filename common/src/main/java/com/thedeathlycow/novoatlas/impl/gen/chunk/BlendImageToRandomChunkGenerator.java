package com.thedeathlycow.novoatlas.impl.gen.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.gen.density.GetPreliminaryHeightFromMapDensityFunction;
import com.thedeathlycow.novoatlas.impl.gen.density.HeightmapDensityFunction;
import com.thedeathlycow.novoatlas.impl.gen.density.BlendAtMapBorder;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.*;
import org.jspecify.annotations.NonNull;

public final class BlendImageToRandomChunkGenerator extends ImageBasedChunkGenerator {
    public static final MapCodec<BlendImageToRandomChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BiomeSource.CODEC
                                    .fieldOf("biome_source")
                                    .forGetter(BlendImageToRandomChunkGenerator::getBiomeSource),
                            NoiseGeneratorSettings.CODEC
                                    .fieldOf("settings")
                                    .forGetter(BlendImageToRandomChunkGenerator::generatorSettings),
                            MapInfo.CODEC
                                    .fieldOf("map_info")
                                    .forGetter(BlendImageToRandomChunkGenerator::getMapInfo),
                            DensityFunction.CODEC
                                    .fieldOf("underground_density_function")
                                    .forGetter(BlendImageToRandomChunkGenerator::getUndergroundDensityFunction),
                            Codec.BOOL
                                    .optionalFieldOf("enable_carvers", true)
                                    .forGetter(BlendImageToRandomChunkGenerator::isEnableCarvers),
                            ExtraCodecs.POSITIVE_FLOAT
                                    .optionalFieldOf("blend_distance", 64f)
                                    .forGetter(BlendImageToRandomChunkGenerator::getBlendDistance)
                    )
                    .apply(instance, BlendImageToRandomChunkGenerator::new)
    );

    private final float blendDistance;

    public BlendImageToRandomChunkGenerator(
            BiomeSource biomeSource,
            Holder<NoiseGeneratorSettings> settings,
            Holder<MapInfo> mapInfo,
            DensityFunction undergroundDensityFunction,
            boolean enableCarvers,
            float blendDistance
    ) {
        super(
                biomeSource,
                applyHeightMapToDensityFunctions(settings, mapInfo, undergroundDensityFunction, blendDistance),
                mapInfo,
                undergroundDensityFunction,
                enableCarvers
        );
        this.blendDistance = blendDistance;
    }

    private static Holder<NoiseGeneratorSettings> applyHeightMapToDensityFunctions(
            Holder<NoiseGeneratorSettings> settings,
            Holder<MapInfo> mapInfo,
            DensityFunction undergroundDensityFunction,
            float blendDistance
    ) {
        final NoiseGeneratorSettings baseSettings = settings.value();
        final NoiseRouter baseNoiseRouter = baseSettings.noiseRouter();
        final NoiseSettings noiseSettings = baseSettings.noiseSettings();

        final int minY = noiseSettings.minY();
        final int maxY = minY + noiseSettings.height();

        DensityFunction preliminaryHeightmap = new GetPreliminaryHeightFromMapDensityFunction(mapInfo, minY, maxY);
        preliminaryHeightmap = new BlendAtMapBorder(mapInfo, preliminaryHeightmap, baseNoiseRouter.preliminarySurfaceLevel(), blendDistance);

        DensityFunction finalDensity = DensityFunctions.min(
                new HeightmapDensityFunction(mapInfo, 128.0),
                undergroundDensityFunction
        );
        finalDensity = new BlendAtMapBorder(mapInfo, finalDensity, baseNoiseRouter.finalDensity(), blendDistance);

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
    protected MapCodec<BlendImageToRandomChunkGenerator> codec() {
        return CODEC;
    }

    public float getBlendDistance() {
        return blendDistance;
    }
}