package com.thedeathlycow.novoatlas.impl.gen.chunk;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.gen.density.HeightmapDensityFunction;
import com.thedeathlycow.novoatlas.impl.gen.density.InitialDensityHeightmapDF;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import com.thedeathlycow.novoatlas.mixin.accessor.NoiseBasedChunkGeneratorAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.*;

public abstract class ImageBasedChunkGenerator extends NoiseBasedChunkGenerator {
    private final Holder<MapInfo> mapInfo;
    private final Holder<DensityFunction> undergroundDensityFunction;
    private final boolean enableCarvers;

    protected ImageBasedChunkGenerator(
            BiomeSource biomeSource,
            Holder<NoiseGeneratorSettings> settings,
            Holder<MapInfo> mapInfo,
            Holder<DensityFunction> undergroundDensityFunction,
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

    public final Holder<DensityFunction> getUndergroundDensityFunction() {
        return undergroundDensityFunction;
    }

    public final boolean isEnableCarvers() {
        return enableCarvers;
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
        if (this.enableCarvers) {
            super.applyCarvers(worldGenRegion, seed, randomState, biomeManager, structureManager, chunkAccess, carving);
        }
    }

    protected int sampleFluidElevation(int x, int z) {
        return this.mapInfo.value().getFluidHeightMapElevation(x, z, this.getSeaLevel());
    }

    @Override
    protected abstract MapCodec<? extends ImageBasedChunkGenerator> codec();

    static DensityFunction createPatchedPreliminaryDensity(final Holder<MapInfo> mapInfo) {
        return new InitialDensityHeightmapDF(mapInfo, 128.f);
    }

    static DensityFunction createPatchedFinalDensity(final Holder<MapInfo> mapInfo, final DensityFunction undergroundDensityFunction) {
        return DensityFunctions.min(
                new HeightmapDensityFunction(mapInfo, 128.0f),
                undergroundDensityFunction
        );
    }

    static Holder<NoiseGeneratorSettings> applyPatchedDensityFunctionsToNoiseSettings(
            final NoiseGeneratorSettings baseSettings,
            final NoiseRouter baseNoiseRouter,
            final DensityFunction chunkSurfaceLevel,
            final DensityFunction finalDensity
    ) {
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
                chunkSurfaceLevel,
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