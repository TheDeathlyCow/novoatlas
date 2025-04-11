package com.thedeathlycow.novoatlas.world.gen;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.NovoAtlas;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class NovoAtlasChunkGenerator extends ChunkGenerator {
    private final MapInfo mapInfo;
    Holder<NoiseGeneratorSettings> settings;
    private final int ceilingHeight;
    private final Supplier<Aquifer.FluidPicker> globalFluidPicker;

    public NovoAtlasChunkGenerator(
            BiomeSource biomeSource,
            Holder<MapInfo> mapInfo,
            Holder<NoiseGeneratorSettings> settings,
            int ceilingHeight
    ) {
        super(biomeSource);
        this.mapInfo = mapInfo.value();
        this.settings = settings;
        this.ceilingHeight = ceilingHeight;
        this.globalFluidPicker = Suppliers.memoize(() -> createFluidPicker(settings.value()));

        if (this.mapInfo.verticalScale() != 1f) {
            NovoAtlas.LOGGER.warn("Using non-standard vertical scale, expect weird generation!");
        }
    }

    private static Aquifer.FluidPicker createFluidPicker(NoiseGeneratorSettings noiseGeneratorSettings) {
        final int lavelLevel = -54;

        Aquifer.FluidStatus lava = new Aquifer.FluidStatus(lavelLevel, Blocks.LAVA.defaultBlockState());
        int seaLevel = noiseGeneratorSettings.seaLevel();
        Aquifer.FluidStatus baseFluid = new Aquifer.FluidStatus(seaLevel, noiseGeneratorSettings.defaultFluid());

        return (x, y, z) -> y < Math.min(lavelLevel, seaLevel) ? lava : baseFluid;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return null;
    }

    @Override
    public void applyCarvers(
            WorldGenRegion worldGenRegion,
            long seed,
            RandomState randomState,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunkAccess
    ) {
        BiomeManager noiseBiomeManager = biomeManager.withDifferentSource(
                (ix, jx, kx) -> this.biomeSource.getNoiseBiome(ix, jx, kx, randomState.sampler())
        );

        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        int i = 8;
        ChunkPos chunkPos = chunkAccess.getPos();
        NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(
                chunkAccessx -> this.createNoiseChunk(chunkAccessx, structureManager, Blender.of(worldGenRegion), randomState)
        );
        Aquifer aquifer = noiseChunk.aquifer();
        CarvingContext carvingContext = new CarvingContext(
                // this is probably fine because its immediately upcast to chunk generator
                new NoiseBasedChunkGenerator(this.biomeSource, this.settings),

                worldGenRegion.registryAccess(),
                chunkAccess.getHeightAccessorForGeneration(),
                noiseChunk,
                randomState,
                this.settings.value().surfaceRule()
        );
        CarvingMask carvingMask = ((ProtoChunk) chunkAccess).getOrCreateCarvingMask();

        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                ChunkPos offset = new ChunkPos(chunkPos.x + dx, chunkPos.z + dz);
                ChunkAccess chunk = worldGenRegion.getChunk(offset.x, offset.z);

                BiomeGenerationSettings biomeGenerationSettings = chunk.carverBiome(
                        () -> this.getBiomeGenerationSettings(
                                this.biomeSource.getNoiseBiome(
                                        QuartPos.fromBlock(offset.getMinBlockX()),
                                        0,
                                        QuartPos.fromBlock(offset.getMinBlockZ()),
                                        randomState.sampler()
                                )
                        )
                );

                int delta = 0;
                for (Holder<ConfiguredWorldCarver<?>> holder : biomeGenerationSettings.getCarvers()) {

                    ConfiguredWorldCarver<?> configuredWorldCarver = holder.value();
                    worldgenRandom.setLargeFeatureSeed(seed + delta, offset.x, offset.z);

                    if (configuredWorldCarver.isStartChunk(worldgenRandom)) {
                        configuredWorldCarver.carve(
                                carvingContext,
                                chunkAccess,
                                noiseBiomeManager::getBiome,
                                worldgenRandom,
                                aquifer,
                                offset,
                                carvingMask
                        );
                    }

                    delta++;
                }
            }
        }
    }

    @Override
    public void buildSurface(
            WorldGenRegion worldGenRegion,
            StructureManager structureManager,
            RandomState randomState,
            ChunkAccess chunkAccess
    ) {
        if (!SharedConstants.debugVoidTerrain(chunkAccess.getPos())) {
            WorldGenerationContext context = new WorldGenerationContext(this, worldGenRegion);
            this.buildSurface(
                    chunkAccess,
                    context,
                    randomState,
                    structureManager,
                    worldGenRegion.getBiomeManager(),
                    worldGenRegion.registryAccess().lookupOrThrow(Registries.BIOME),
                    Blender.of(worldGenRegion)
            );
        }
    }

    private void buildSurface(
            ChunkAccess chunkAccess,
            WorldGenerationContext worldGenerationContext,
            RandomState randomState,
            StructureManager structureManager,
            BiomeManager biomeManager,
            Registry<Biome> registry,
            Blender blender
    ) {
        NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(chunkAccessx -> this.createNoiseChunk(chunkAccessx, structureManager, blender, randomState));
        NoiseGeneratorSettings noiseGeneratorSettings = this.settings.value();
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
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {

    }

    @Override
    public int getGenDepth() {
        return 0;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
        return null;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return null;
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {

    }

    private NoiseChunk createNoiseChunk(ChunkAccess chunkAccess, StructureManager structureManager, Blender blender, RandomState randomState) {
        return NoiseChunk.forChunk(
                chunkAccess,
                randomState,
                Beardifier.forStructuresInChunk(structureManager, chunkAccess.getPos()),
                this.settings.value(),
                this.globalFluidPicker.get(),
                blender
        );
    }
}