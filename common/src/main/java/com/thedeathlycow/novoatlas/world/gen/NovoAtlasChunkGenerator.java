package com.thedeathlycow.novoatlas.world.gen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.NovoAtlas;
import com.thedeathlycow.novoatlas.mixin.accessor.NoiseBasedChunkGeneratorAccessor;
import com.thedeathlycow.novoatlas.mixin.accessor.NoiseChunkAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
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

import java.util.OptionalInt;

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
        return (int) this.getFromMap(x, z, this.mapInfo.value().lookupHeightmap());
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
            return ((NoiseBasedChunkGeneratorAccessor) this).invokeCreateNoiseChunk(
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
                c -> ((NoiseBasedChunkGeneratorAccessor) this).invokeCreateNoiseChunk(
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
                        int absoluteY = (minCellY + localY) * cellHeight + localY;
                        int localBlockY = absoluteY & 0xF;

                        int sectionIndex = chunkAccess.getSectionIndex(absoluteY);
                        if (section != sectionIndex) {
                            section = sectionIndex;
                            currentSection = chunkAccess.getSection(sectionIndex);
                        }

                        double deltaY = (double) localY / cellHeight;
                        noiseChunk.updateForY(absoluteY, deltaY);

                        blockX:
                        for (int localX = 0; localX < cellWidth; localX++) {
                            int absoluteX = chunkBlockX + cellX * cellWidth + localX;
                            int localBlockX = absoluteX & 0xF;

                            double deltaX = (double) localX / cellWidth;
                            noiseChunk.updateForX(absoluteX, deltaX);

                            blockZ:
                            // NOSONAR: labels are necessary here for clarity
                            for (int localZ = 0; localZ < cellWidth; localZ++) {
                                int absoluteZ = chunkBlockZ + cellZ * cellWidth + localZ;
                                int localBlockZ = absoluteZ & 0xF;

                                double deltaZ = (double) localZ / cellWidth;
                                noiseChunk.updateForZ(absoluteZ, deltaZ);

                                // sample from heightmap
                                int x = noiseChunk.blockX();
                                int y = noiseChunk.blockY();
                                int z = noiseChunk.blockZ();

                                mutable.set(x, y, z);

                                OptionalInt maybeElevation = this.sampleHeight(
                                        randomState,
                                        noiseChunk,
                                        x, y, z
                                );

                                if (maybeElevation.isEmpty()) {
                                    continue blockZ;
                                }
                                int elevation = maybeElevation.getAsInt();
                                BlockState state;

                                if (elevation - y <= 10) {
                                    state = this.sampleBlockStateForHeight(
                                            randomState,
                                            noiseChunk,
                                            absoluteY,
                                            elevation
                                    );
                                } else {
                                    // fallback to vanilla sampling when below surface
                                    state = this.sampleState(noiseChunkAccessor);
                                }

                                if (!state.isAir() && !SharedConstants.debugVoidTerrain(chunkAccess.getPos())) {
                                    currentSection.setBlockState(localBlockX, localBlockY, localBlockZ, state, false);

                                    oceanFloor.update(localBlockX, absoluteY, localBlockZ, state);
                                    worldSurface.update(localBlockX, absoluteY, localBlockZ, state);
                                }

                                if (aquifer.shouldScheduleFluidUpdate() && !state.getFluidState().isEmpty()) {
                                    mutable.set(absoluteX, absoluteY, absoluteZ);
                                    chunkAccess.markPosForPostprocessing(mutable);
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

    private OptionalInt sampleHeight(RandomState randomState, NoiseChunk noiseChunk, int x, int y, int z) {
        MapInfo info = this.mapInfo.value();
        int seaLevel = this.getSeaLevel();
        int elevation = Mth.floor(
                Math.min(
                        this.getFromMap(x, z, info.lookupHeightmap()),
                        (double) info.startingY() + this.getGenDepth()
                )
        );

        int minY = this.getMinY();
        if (y >= seaLevel && y >= elevation || elevation < minY) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(elevation);
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

    private double getFromMap(int x, int z, @NotNull MapImage mapImage) {
        MapInfo info = this.mapInfo.value();

        float xR = (x / info.horizontalScale()) + mapImage.width() / 2f; // these will always be even numbers
        float zR = (z / info.horizontalScale()) + mapImage.height() / 2f;

        if (xR < 0 || zR < 0 || xR >= mapImage.width() || zR >= mapImage.height()) {
            return this.getMinY() - 1;
        }

        int truncatedX = Mth.floor(xR);
        int truncatedZ = Mth.floor(zR);

        double height = mapImage.bilerp(truncatedX, xR - truncatedX, truncatedZ, zR - truncatedZ);

        return info.verticalScale() * height + info.startingY();
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
        return this.generatorSettings().value().defaultBlock();
    }

    private BlockState defaultBlock() {
        return this.generatorSettings().value().defaultBlock();
    }
}