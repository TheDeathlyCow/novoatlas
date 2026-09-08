package com.thedeathlycow.novoatlas.impl.gen;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import com.thedeathlycow.novoatlas.mixin.accessor.NoiseBasedChunkGeneratorAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.*;

public class ImageMapChunkGenerator extends NoiseBasedChunkGenerator {
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

    private final Holder<MapInfo> mapInfo;

    private final DensityFunction undergroundDensityFunction;

    private final boolean enableCarvers;

    public ImageMapChunkGenerator(
            BiomeSource biomeSource,
            Holder<NoiseGeneratorSettings> settings,
            Holder<MapInfo> mapInfo,
            DensityFunction undergroundDensityFunction,
            boolean enableCarvers
    ) {
        super(
                biomeSource,
                applyHeightMapToDensityFunctions(settings, mapInfo, undergroundDensityFunction)
        );

        this.mapInfo = mapInfo;
        this.undergroundDensityFunction = undergroundDensityFunction;
        this.enableCarvers = enableCarvers;
        ((NoiseBasedChunkGeneratorAccessor) this).novoatlas$setGlobalFluidPicker(Suppliers.memoize(() -> this.pickFluid(settings.value())));
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

    private static Holder<NoiseGeneratorSettings> applyHeightMapToDensityFunctions(
            Holder<NoiseGeneratorSettings> settings,
            Holder<MapInfo> mapInfo,
            DensityFunction undergroundDensityFunction
    ) {
        NoiseGeneratorSettings baseSettings = settings.value();

        NoiseRouter baseNoiseRouter = baseSettings.noiseRouter();

        DensityFunction heightMap = new HeightmapDensityFunction(mapInfo);

        NoiseSettings noiseSettings = baseSettings.noiseSettings();
        int minY = noiseSettings.minY();
        int maxY = minY + noiseSettings.height();

        DensityFunction preliminaryHeightmap = new GetPreliminaryHeightFromMapDensityFunction(mapInfo, minY, maxY);

        DensityFunction finalDensity = DensityFunctions.min(
                undergroundDensityFunction,
                heightMap
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
    protected MapCodec<? extends ImageMapChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk) {
        if (this.enableCarvers) {
            super.applyCarvers(level, seed, random, biomeManager, structureManager, chunk);
        }
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return this.sampleElevation(x, z);
    }

    private int sampleElevation(int x, int z) {
        return this.mapInfo.value().getHeightMapElevation(x, z);
    }

    private int sampleFluidElevation(int x, int z) {
        return this.mapInfo.value().getFluidHeightMapElevation(x, z, this.getSeaLevel());
    }

    public Holder<MapInfo> getMapInfo() {
        return mapInfo;
    }

    public DensityFunction getUndergroundDensityFunction() {
        return undergroundDensityFunction;
    }

    public boolean isEnableCarvers() {
        return enableCarvers;
    }
}