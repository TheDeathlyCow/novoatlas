package com.thedeathlycow.novoatlas.world.gen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.mixin.accessor.NoiseChunkAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;

public class BoundedMapChunkGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<BoundedMapChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BiomeSource.CODEC
                                    .fieldOf("biome_source")
                                    .forGetter(BoundedMapChunkGenerator::getBiomeSource),
                            NoiseGeneratorSettings.CODEC
                                    .fieldOf("settings")
                                    .forGetter(BoundedMapChunkGenerator::generatorSettings),
                            MapInfo.CODEC
                                    .fieldOf("map_info")
                                    .forGetter(BoundedMapChunkGenerator::getMapInfo)
                    )
                    .apply(instance, BoundedMapChunkGenerator::new)
    );

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private final Holder<MapInfo> mapInfo;

    public BoundedMapChunkGenerator(
            BiomeSource biomeSource,
            Holder<NoiseGeneratorSettings> settings,
            Holder<MapInfo> mapInfo
    ) {
        super(biomeSource, settings);
        this.mapInfo = mapInfo;
    }

    @Override
    protected MapCodec<? extends BoundedMapChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return this.sampleElevation(x, z);
    }

    /**
     * A debofuscated reimplementation of {@link NoiseBasedChunkGenerator#doFill(Blender, StructureManager, RandomState, ChunkAccess, int, int)}
     * (also called populateNoise in Yarn).
     * <p>
     * This implementation is very similar to the vanilla one - except that it contains special handling for sampling from
     * the atlas height map (which is part of this mod).
     */
    @Override
    protected ChunkAccess doFill(
            Blender blender,
            StructureManager structureManager,
            RandomState randomState,
            ChunkAccess chunkAccess,
            int minCellY,
            int noiseCellCount
    ) {
        NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(
                c -> this.createNoiseChunk(
                        c,
                        structureManager,
                        blender,
                        randomState
                )
        );

        NoiseChunkAccessor noiseChunkAccessor = (NoiseChunkAccessor) noiseChunk;

        // used for beardifying structures
        DensityFunction finalDensity = DensityFunctions.cacheAllInCell(
                randomState.router()
                        .mapAll(noiseChunkAccessor::invokeWrap)
                        .finalDensity()
        ).mapAll(noiseChunkAccessor::invokeWrap);
        DensityFunction beardifier = DensityFunctions.cacheAllInCell(noiseChunkAccessor.accessBeardifier())
                .mapAll(noiseChunkAccessor::invokeWrap);


        Heightmap oceanFloor = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        ChunkPos chunkPos = chunkAccess.getPos();
        int chunkBlockX = chunkPos.getMinBlockX();
        int chunkBlockZ = chunkPos.getMinBlockZ();

        Aquifer aquifer = noiseChunk.aquifer();

        noiseChunk.initializeForFirstCellX();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        // cells = resolution at which noise is sampled, must be between 1 and 4
        int cellWidth = noiseChunkAccessor.invokeCellWidth();
        int cellHeight = noiseChunkAccessor.invokeCellHeight();

        int cellsPerChunkX = 16 / cellWidth;
        int cellsPerChunkZ = 16 / cellWidth;

        // iterate over cells within the chunk
        cellX:
        for (int cellX = 0; cellX < cellsPerChunkX; cellX++) {
            noiseChunk.advanceCellX(cellX);

            cellZ:
            for (int cellZ = 0; cellZ < cellsPerChunkZ; cellZ++) {
                int section = chunkAccess.getSectionsCount() - 1;
                LevelChunkSection currentSection = chunkAccess.getSection(section);

                cellY:
                for (int cellY = noiseCellCount - 1; cellY >= 0; cellY--) {
                    noiseChunk.selectCellYZ(cellY, cellZ);

                    // iterate over each block in the cell
                    blockY:
                    for (int localY = cellHeight - 1; localY >= 0; localY--) {
                        int absoluteY = (minCellY + cellY) * cellHeight + localY;
                        int localBlockY = absoluteY & 0xF;

                        int sectionIndex = chunkAccess.getSectionIndex(absoluteY);
                        if (section != sectionIndex) {
                            section = sectionIndex;
                            currentSection = chunkAccess.getSection(sectionIndex);
                        }

                        noiseChunk.updateForY(absoluteY, (double) localY / cellHeight);

                        blockX:
                        for (int localX = 0; localX < cellWidth; localX++) {
                            int absoluteX = chunkBlockX + cellX * cellWidth + localX;
                            int localBlockX = absoluteX & 0xF;

                            noiseChunk.updateForX(absoluteX, (double) localX / cellWidth);

                            blockZ:
                            for (int localZ = 0; localZ < cellWidth; localZ++) {
                                int absoluteZ = chunkBlockZ + cellZ * cellWidth + localZ;
                                int localBlockZ = absoluteZ & 0xF;

                                noiseChunk.updateForZ(absoluteZ, (double) localZ / cellWidth);

                                // sample from heightmap
                                int elevation = this.sampleElevation(absoluteX, absoluteZ);

                                // todo: end of the world generation
                                if (elevation < this.getMinY()) {
                                    continue blockZ;
                                }

                                BlockState state = this.sampleState(noiseChunk);

                                if (!state.is(Blocks.AIR) && !SharedConstants.debugVoidTerrain(chunkAccess.getPos())) {
                                    currentSection.setBlockState(localBlockX, localBlockY, localBlockZ, state, false);

                                    oceanFloor.update(localBlockX, absoluteY, localBlockZ, state);
                                    worldSurface.update(localBlockX, absoluteY, localBlockZ, state);

                                    if (aquifer.shouldScheduleFluidUpdate() && !state.getFluidState().isEmpty()) {
                                        mutable.set(absoluteX, absoluteY, absoluteZ);
                                        chunkAccess.markPosForPostprocessing(mutable);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            noiseChunk.swapSlices();
        }

        noiseChunk.stopInterpolation();
        return chunkAccess;
    }

    private int sampleElevation(int x, int z) {
        return this.mapInfo.value().getHeightMapElevation(x, z, this.getMinY() - 1);
    }

    /**
     * Computes density for structure adapters. If a beardifier wants to apply an adaption, it will return that adaption
     * plus the final density (adding final density softens the edges).
     *
     * @deprecated Replaced with a density function type that handles this more elegantly, and incoporates other
     * features like fluids better. Will be kept for posterity as long as it continues to compile.
     */
    @Deprecated
    private double computeBeardDensity(int distanceBelowTop, double finalDensity, DensityFunction beardifier, NoiseChunk noiseChunk) {
        double beard = beardifier.compute(noiseChunk);
        if (beard > 0) {
            double softening = distanceBelowTop >= 0
                    ? finalDensity
                    : distanceBelowTop * 0.025;

            return softening + beard;
        } else {
            return -1;
        }
    }

    public Holder<MapInfo> getMapInfo() {
        return mapInfo;
    }

    private BlockState sampleState(NoiseChunk noiseChunk) {
        BlockState state = ((NoiseChunkAccessor) noiseChunk).invokeGetInterpolatedState();

        if (state == null) {
            return this.defaultBlock();
        }

        return state;
    }

    private BlockState defaultFluid() {
        return this.generatorSettings().value().defaultFluid();
    }

    private BlockState defaultBlock() {
        return this.generatorSettings().value().defaultBlock();
    }

}