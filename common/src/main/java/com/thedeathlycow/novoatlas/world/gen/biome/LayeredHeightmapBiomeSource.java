package com.thedeathlycow.novoatlas.world.gen.biome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class LayeredHeightmapBiomeSource extends BiomeSource {
    public static final MapCodec<LayeredHeightmapBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(LayeredHeightmapBiomeSource::getMapInfo),
                    Biome.CODEC
                            .fieldOf("default_biome")
                            .forGetter(LayeredHeightmapBiomeSource::getDefaultBiome),
                    BiomeLayerEntry.CODEC.listOf()
                            .fieldOf("layers")
                            .forGetter(LayeredHeightmapBiomeSource::getLayers)
            ).apply(instance, LayeredHeightmapBiomeSource::new)
    );

    private final Holder<MapInfo> mapInfo;
    private final Holder<Biome> defaultBiome;
    private final List<BiomeLayerEntry> layers;

    public LayeredHeightmapBiomeSource(Holder<MapInfo> mapInfo, Holder<Biome> defaultBiome, List<BiomeLayerEntry> layers) {
        this.mapInfo = mapInfo;
        this.defaultBiome = defaultBiome;
        this.layers = layers;
    }

    @Override
    @NotNull
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    @NotNull
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        Stream<Holder<Biome>> layersSteam = this.layers.stream()
                .map(BiomeLayerEntry::biomeSource)
                .flatMap(biomeSource -> biomeSource.possibleBiomes().stream());

        return Stream.concat(Stream.of(defaultBiome), layersSteam);
    }

    @Override
    @NotNull
    public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler sampler) {
        MapInfo info = this.mapInfo.value();
        int x = QuartPos.toBlock(biomeX);
        int y = QuartPos.toBlock(biomeY);
        int z = QuartPos.toBlock(biomeZ);

        int elevation = info.getHeightMapElevation(x, z, -1);
        if (elevation == -1) {
            return this.defaultBiome;
        }

        BiomeLayerEntry layer = this.getLayer(elevation - y);

        return layer != null ? layer.biomeSource().getNoiseBiome(biomeX, biomeY, biomeZ, sampler) : this.defaultBiome;
    }

    @Nullable
    private BiomeLayerEntry getLayer(int offset) {
        for (BiomeLayerEntry layer : this.layers) {
            if (layer.isInLayer(offset)) {
                return layer;
            }
        }

        return null;
    }

    public Holder<MapInfo> getMapInfo() {
        return mapInfo;
    }

    public Holder<Biome> getDefaultBiome() {
        return defaultBiome;
    }

    public List<BiomeLayerEntry> getLayers() {
        return layers;
    }
}