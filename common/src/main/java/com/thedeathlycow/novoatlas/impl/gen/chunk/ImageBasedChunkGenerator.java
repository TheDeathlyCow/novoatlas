package com.thedeathlycow.novoatlas.impl.gen.chunk;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
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
import org.jspecify.annotations.NonNull;

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

    public Holder<MapInfo> getMapInfo() {
        return mapInfo;
    }

    public DensityFunction getUndergroundDensityFunction() {
        return undergroundDensityFunction;
    }

    public boolean isEnableCarvers() {
        return enableCarvers;
    }

    protected int sampleElevation(int x, int z) {
        return this.mapInfo.value().getHeightMapElevation(x, z);
    }

    protected int sampleFluidElevation(int x, int z) {
        return this.mapInfo.value().getFluidHeightMapElevation(x, z, this.getSeaLevel());
    }

    @Override
    @NonNull
    protected abstract MapCodec<? extends ImageBasedChunkGenerator> codec();
}