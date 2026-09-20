package com.thedeathlycow.novoatlas.impl.gen.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.gen.density.BlendAtMapBorder;
import com.thedeathlycow.novoatlas.impl.gen.density.GetPreliminaryHeightFromMapDensityFunction;
import com.thedeathlycow.novoatlas.impl.gen.density.HeightmapDensityFunction;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

@ApiStatus.Experimental
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

        DensityFunction chunkSurfaceLevelHeightmap = new GetPreliminaryHeightFromMapDensityFunction(mapInfo, minY, maxY);
        chunkSurfaceLevelHeightmap = new BlendAtMapBorder(mapInfo, chunkSurfaceLevelHeightmap, baseNoiseRouter.chunkSurfaceLevel(), blendDistance);

        DensityFunction finalDensity = DensityFunctions.min(
                new HeightmapDensityFunction(mapInfo, 128.0f),
                undergroundDensityFunction
        );
        finalDensity = new BlendAtMapBorder(mapInfo, finalDensity, baseNoiseRouter.finalDensity(), blendDistance);

        Aquifer.Config aquifers = baseSettings.aquifers().orElse(null);
        if (aquifers != null) {
            aquifers = new Aquifer.Config(
                    aquifers.barrierNoise(),
                    aquifers.fluidLevelFloodednessNoise(),
                    aquifers.fluidLevelSpreadNoise(),
                    aquifers.lavaNoise(),
                    aquifers.exclusion(),
                    chunkSurfaceLevelHeightmap
            );
        }

        NoiseRouter fixedNoiseRouter = new NoiseRouter(
                baseNoiseRouter.temperature(),
                baseNoiseRouter.vegetation(),
                baseNoiseRouter.continents(),
                baseNoiseRouter.erosion(),
                baseNoiseRouter.depth(),
                baseNoiseRouter.ridges(),
                chunkSurfaceLevelHeightmap,
                finalDensity
        );

        NoiseGeneratorSettings fixedSettings = new NoiseGeneratorSettings(
                baseSettings.noiseSettings(),
                baseSettings.defaultBlock(),
                baseSettings.defaultFluid(),
                fixedNoiseRouter,
                baseSettings.materialRule(),
                baseSettings.spawnTarget(),
                baseSettings.seaLevel(),
                baseSettings.disableMobGeneration(),
                Optional.ofNullable(aquifers),
                baseSettings.useLegacyRandomSource(),
                baseSettings.debugFunctions()
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

    @Override
    protected int sampleFluidElevation(int x, int z) {
        if (this.getMapInfo().value().isBlockInsideHeightMap(x, z)) {
            return super.sampleFluidElevation(x, z);
        } else {
            return this.getSeaLevel();
        }
    }
}