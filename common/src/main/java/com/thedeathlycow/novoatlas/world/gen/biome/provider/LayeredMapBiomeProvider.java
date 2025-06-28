package com.thedeathlycow.novoatlas.world.gen.biome.provider;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import com.thedeathlycow.novoatlas.world.gen.biome.BiomeLayerEntry;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class LayeredMapBiomeProvider implements BiomeMapProvider {
    public static final MapCodec<LayeredMapBiomeProvider> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BiomeLayerEntry.CODEC.listOf()
                            .fieldOf("layers")
                            .forGetter(LayeredMapBiomeProvider::getLayers)
            ).apply(instance, LayeredMapBiomeProvider::new)
    );

    private final List<BiomeLayerEntry> layers;

    public LayeredMapBiomeProvider(List<BiomeLayerEntry> layers) {
        this.layers = layers;
    }

    @Override
    @Nullable
    public Holder<Biome> getBiome(int x, int y, int z, MapInfo info) {
        int elevation = info.getHeightMapElevation(x, z, Integer.MIN_VALUE);

        if (elevation == Integer.MIN_VALUE) {
            return null;
        }

        BiomeLayerEntry layer = this.getLayer(elevation - y);
        return layer != null ? layer.biomeProvider().getBiome(x, y, z, info) : null;
    }

    @Override
    public Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.layers.stream()
                .map(BiomeLayerEntry::biomeProvider)
                .flatMap(ColorMapBiomeProvider::collectPossibleBiomes);
    }

    @Override
    public MapCodec<? extends LayeredMapBiomeProvider> getCodec() {
        return CODEC;
    }

    public List<BiomeLayerEntry> getLayers() {
        return layers;
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
}