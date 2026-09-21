package com.thedeathlycow.novoatlas.impl.gen.chunk;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.gen.density.GetPreliminaryHeightFromMapDensityFunction;
import com.thedeathlycow.novoatlas.impl.gen.density.HeightmapDensityFunction;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import com.thedeathlycow.novoatlas.mixin.accessor.NoiseBasedChunkGeneratorAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public abstract class ImageBasedChunkGenerator extends NoiseBasedChunkGenerator {
    private final Holder<MapInfo> mapInfo;
    private final DensityFunction undergroundDensityFunction;
    private final boolean enableCarvers;

    protected ImageBasedChunkGenerator(
            BiomeSource biomeSource,
            Holder<NoiseGeneratorSettings> settings,
            Holder<MapInfo> mapInfo,
            DensityFunction undergroundDensityFunction,
            boolean enableCarvers
    ) {
        super(biomeSource, settings);
        this.mapInfo = mapInfo;
        this.undergroundDensityFunction = undergroundDensityFunction;
        this.enableCarvers = enableCarvers;

        ((NoiseBasedChunkGeneratorAccessor) this).novoatlas$setGlobalFluidPicker(Suppliers.memoize(() -> this.pickFluid(this.generatorSettings().value())));
    }

    public final Holder<MapInfo> getMapInfo() {
        return mapInfo;
    }

    public final DensityFunction getUndergroundDensityFunction() {
        return undergroundDensityFunction;
    }

    public final boolean isEnableCarvers() {
        return enableCarvers;
    }

    @Override
    protected final void generateCarvers(final ChunkAccess chunk, final Blender blender, final NoiseChunk noiseChunk, final RandomState randomState, final BiomeManager biomeManager, final @Nullable WorldGenRegion carverBiomeRegion, final MaterialRule materialRule) {
        if (this.enableCarvers) {
            super.generateCarvers(chunk, blender, noiseChunk, randomState, biomeManager, carverBiomeRegion, materialRule);
        }
    }

    protected int sampleFluidElevation(int x, int z) {
        return this.mapInfo.value().getFluidHeightMapElevation(x, z, this.getSeaLevel());
    }

    @Override
    @NonNull
    protected abstract MapCodec<? extends ImageBasedChunkGenerator> codec();

    static DensityFunction createPatchedPreliminaryDensity(final Holder<MapInfo> mapInfo, final NoiseSettings noiseSettings) {
        final int minY = noiseSettings.minY();
        final int maxY = minY + noiseSettings.height();
        return new GetPreliminaryHeightFromMapDensityFunction(mapInfo, minY, maxY);
    }

    static DensityFunction createPatchedFinalDensity(final Holder<MapInfo> mapInfo, final DensityFunction undergroundDensityFunction) {
        return DensityFunctions.add(
                DensityFunctions.min(
                        new HeightmapDensityFunction(mapInfo, 128.0f),
                        undergroundDensityFunction
                ),
                DensityFunctions.beardifier()
        );
    }

    @NonNull
    static Holder<NoiseGeneratorSettings> applyPatchedDensityFunctionsToNoiseSettings(
            final NoiseGeneratorSettings baseSettings,
            final NoiseRouter baseNoiseRouter,
            final DensityFunction chunkSurfaceLevel,
            final DensityFunction finalDensity
    ) {
        Aquifer.Config aquifers = baseSettings.aquifers().orElse(null);
        if (aquifers != null) {
            aquifers = new Aquifer.Config(
                    aquifers.barrierNoise(),
                    aquifers.fluidLevelFloodednessNoise(),
                    aquifers.fluidLevelSpreadNoise(),
                    aquifers.lavaNoise(),
                    aquifers.exclusion(),
                    chunkSurfaceLevel
            );
        }

        NoiseRouter fixedNoiseRouter = new NoiseRouter(
                baseNoiseRouter.temperature(),
                baseNoiseRouter.vegetation(),
                baseNoiseRouter.continents(),
                baseNoiseRouter.erosion(),
                baseNoiseRouter.depth(),
                baseNoiseRouter.ridges(),
                chunkSurfaceLevel,
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

    private Aquifer.FluidPicker pickFluid(NoiseGeneratorSettings settings) {
        Aquifer.FluidStatus lava = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        int seaLevel = settings.seaLevel();
        Aquifer.FluidStatus air = new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());

        return (x, y, z) -> {
            if (SharedConstants.DEBUG_DISABLE_FLUID_GENERATION) {
                return air;
            } else if (y < Math.min(-54, seaLevel)) {
                return lava;
            } else {
                return new Aquifer.FluidStatus(this.sampleFluidElevation(x, z), settings.defaultFluid());
            }
        };
    }
}