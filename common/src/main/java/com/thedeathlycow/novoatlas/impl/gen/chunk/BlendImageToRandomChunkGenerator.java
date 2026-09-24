package com.thedeathlycow.novoatlas.impl.gen.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.gen.density.BlendAtMapBorder;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.jetbrains.annotations.ApiStatus;
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
            Holder<DensityFunction> undergroundDensityFunction,
            boolean enableCarvers,
            float blendDistance
    ) {
        super(
                biomeSource,
                applyHeightMapToDensityFunctions(settings.value(), mapInfo, undergroundDensityFunction.value(), blendDistance),
                mapInfo,
                undergroundDensityFunction,
                enableCarvers
        );
        this.blendDistance = blendDistance;
    }

    private static Holder<NoiseGeneratorSettings> applyHeightMapToDensityFunctions(
            NoiseGeneratorSettings baseSettings,
            Holder<MapInfo> mapInfo,
            DensityFunction undergroundDensityFunction,
            float blendDistance
    ) {
        final NoiseRouter baseNoiseRouter = baseSettings.noiseRouter();

        DensityFunction chunkSurfaceLevel = createPatchedPreliminaryDensity(mapInfo);
        chunkSurfaceLevel = new BlendAtMapBorder(mapInfo, chunkSurfaceLevel, baseNoiseRouter.initialDensityWithoutJaggedness(), blendDistance);

        DensityFunction finalDensity = createPatchedFinalDensity(mapInfo, undergroundDensityFunction);
        finalDensity = new BlendAtMapBorder(mapInfo, finalDensity, baseNoiseRouter.finalDensity(), blendDistance);

        return applyPatchedDensityFunctionsToNoiseSettings(baseSettings, baseNoiseRouter, chunkSurfaceLevel, finalDensity);
    }

    @Override
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