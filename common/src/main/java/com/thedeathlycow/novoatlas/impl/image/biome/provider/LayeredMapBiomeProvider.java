package com.thedeathlycow.novoatlas.impl.image.biome.provider;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import com.thedeathlycow.novoatlas.impl.image.biome.BiomeLayerEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public record LayeredMapBiomeProvider(
        List<BiomeLayerEntry> layers
) implements BiomeMapProvider {
    public static final ResourceKey<Biome> SURFACE_BIOME = ResourceKey.create(
            Registries.BIOME,
            NovoAtlas.id("surface_biome")
    );

    public static final MapCodec<LayeredMapBiomeProvider> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BiomeLayerEntry.CODEC.listOf()
                            .fieldOf("layers")
                            .forGetter(LayeredMapBiomeProvider::layers)
            ).apply(instance, LayeredMapBiomeProvider::new)
    );

    @Override
    @Nullable
    public Holder<Biome> getBiome(int x, int y, int z, MapInfo info) {
        Holder<Biome> colorMapBiome = this.getBiomeFromColorMap(x, y, z, info);

        return colorMapBiome != null && !colorMapBiome.is(SURFACE_BIOME) ? colorMapBiome : null;
    }

    @Override
    public Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.layers.stream()
                .map(BiomeLayerEntry::biomeProvider)
                .flatMap(ColorMapBiomeProvider::collectPossibleBiomes);
    }

    @Override
    public MapCodec<LayeredMapBiomeProvider> getCodec() {
        return CODEC;
    }

    @Nullable
    private BiomeLayerEntry getLayer(int y) {
        for (BiomeLayerEntry layer : this.layers) {
            if (layer.isInLayer(y)) {
                return layer;
            }
        }

        return null;
    }

    private Holder<Biome> getBiomeFromColorMap(int x, int y, int z, MapInfo info) {
        int elevation = info.getHeightMapElevation(x, z, Integer.MIN_VALUE);

        if (elevation == Integer.MIN_VALUE) {
            return null;
        }

        BiomeLayerEntry layer = this.getLayer(y);
        return layer != null ? layer.biomeProvider().getBiome(x, y, z, info) : null;
    }
}