package com.thedeathlycow.novoatlas.world.gen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.NovoAtlas;
import com.thedeathlycow.novoatlas.mixin.accessor.NoiseChunkAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class NovoAtlasChunkGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<NovoAtlasChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BiomeSource.CODEC
                                    .fieldOf("biome_source")
                                    .forGetter(NovoAtlasChunkGenerator::getBiomeSource),
                            NoiseGeneratorSettings.CODEC
                                    .fieldOf("settings")
                                    .forGetter(NovoAtlasChunkGenerator::generatorSettings),
                            MapInfo.CODEC
                                    .fieldOf("map_info")
                                    .forGetter(NovoAtlasChunkGenerator::getMapInfo)
                    )
                    .apply(instance, NovoAtlasChunkGenerator::new)
    );

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private final Holder<MapInfo> mapInfo;

    public NovoAtlasChunkGenerator(
            BiomeSource biomeSource,
            Holder<NoiseGeneratorSettings> settings,
            Holder<MapInfo> mapInfo
    ) {
        super(biomeSource, settings);
        this.mapInfo = mapInfo;

        if (this.mapInfo.value().verticalScale() != 1f) {
            NovoAtlas.LOGGER.warn("Using non-standard vertical scale, expect weird generation!");
        }
    }

    @Override
    protected MapCodec<? extends NovoAtlasChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return this.sampleElevation(x, z);
    }

    @Override
    public void buildSurface(
            ChunkAccess chunkAccess,
            WorldGenerationContext worldGenerationContext,
            RandomState randomState,
            StructureManager structureManager,
            BiomeManager biomeManager,
            Registry<Biome> registry,
            Blender blender
    ) {
        NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(chunk -> {
            return this.createNoiseChunk(
                    chunk,
                    structureManager,
                    blender,
                    randomState
            );
        });

        NoiseGeneratorSettings noiseGeneratorSettings = this.generatorSettings().value();
        ((NovoAtlasSurfaceSystem) randomState.surfaceSystem())
                .novoatlas$buildSurface(
                        randomState,
                        biomeManager,
                        registry,
                        noiseGeneratorSettings.useLegacyRandomSource(),
                        worldGenerationContext,
                        chunkAccess,
                        noiseChunk,
                        noiseGeneratorSettings.surfaceRule(),
                        this.mapInfo
                );
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

                                // sample from heightmap - vanilla would fallback to the regular sampleState() call at
                                // this point
                                int elevation = this.sampleElevation(absoluteX, absoluteZ);

                                if (elevation < this.getMinY()) {
                                    continue blockZ;
                                }

                                BlockState state;
                                if (absoluteY >= elevation - 10) {
                                    state = this.sampleBlockStateForHeight(
                                            randomState,
                                            noiseChunk,
                                            absoluteY,
                                            elevation
                                    );
                                } else {
                                    // use vanilla sampling when below the surface
                                    // this is used in conjunction with custom noise settings to properly fill the interior,
                                    // using the overworld settings here will make hollow mountains
                                    state = this.sampleState(noiseChunkAccessor);
                                }

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

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        int elevation = this.sampleElevation(x, z);
        int minY = this.getMinY();
        int seaLevel = this.getSeaLevel();

        NoiseColumn column;
        if (elevation < minY) {
            column = new NoiseColumn(levelHeightAccessor.getMinY(), new BlockState[]{AIR});
        } else if (elevation < seaLevel) {
            column = new NoiseColumn(
                    minY,
                    Stream.concat(
                            Stream.generate(this::defaultBlock).limit(elevation - minY),
                            Stream.generate(this::defaultFluid).limit(seaLevel - elevation - minY)
                    ).toArray(BlockState[]::new)
            );
        } else {
            column = new NoiseColumn(
                    minY,
                    Stream.generate(this::defaultBlock)
                            .limit(elevation - minY + 1)
                            .toArray(BlockState[]::new)
            );
        }

        return column;
    }

    @Override
    @NotNull
    protected NoiseChunk createNoiseChunk(ChunkAccess chunkAccess, StructureManager structureManager, Blender blender, RandomState randomState) {
        return NoiseChunk.forChunk(
                chunkAccess,
                randomState,
                Beardifier.forStructuresInChunk(structureManager, chunkAccess.getPos()),
                this.generatorSettings().value(),
                this.createFluidLevelSampler(this.generatorSettings().value()),
                blender
        );
    }

    private Aquifer.FluidPicker createFluidLevelSampler(NoiseGeneratorSettings settings) {
        final int lavaLevel = -54;
        var fluidPicker = new Aquifer.FluidStatus(lavaLevel, Blocks.LAVA.defaultBlockState());
        int seaLevel = settings.seaLevel();

        return (x, y, z) -> {
            if (y < Math.min(lavaLevel, seaLevel)) {
                return fluidPicker;
            }
            return new Aquifer.FluidStatus(
                    seaLevel,
                    settings.defaultFluid()
            );
        };
    }

    private int sampleElevation(int x, int z) {
        return this.mapInfo.value().getHeightMapElevation(x, z, this.getMinY() - 1);
    }

    private BlockState sampleBlockStateForHeight(RandomState randomState, NoiseChunk noiseChunk, int y, int elevation) {
        if (y < elevation) {
            double cave = randomState.router()
                    .initialDensityWithoutJaggedness()
                    .compute(noiseChunk);

            return cave > 0
                    ? this.defaultBlock()
                    : this.getCaveState(elevation, this.getSeaLevel());
        } else if (y < this.getSeaLevel()) {
            return this.defaultFluid();
        } else {
            return AIR;
        }
    }

    public Holder<MapInfo> getMapInfo() {
        return mapInfo;
    }

    private BlockState getCaveState(int elevation, int seaLevel) {
        return elevation < seaLevel
                ? this.defaultFluid()
                : Blocks.CAVE_AIR.defaultBlockState();
    }

    private BlockState sampleState(NoiseChunkAccessor noiseChunk) {
        BlockState state = noiseChunk.invokeGetInterpolatedState();

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