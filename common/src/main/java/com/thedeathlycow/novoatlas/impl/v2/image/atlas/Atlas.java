package com.thedeathlycow.novoatlas.impl.v2.image.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.BiomeMap;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.HeightMap;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.biome.BiomeMapLayer;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ColorImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.ImageTransform;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class Atlas {
    public static final Codec<Atlas> DIRECT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ImageTransform.CODEC
                            .optionalFieldOf("transform", ImageTransform.DEFAULT)
                            .forGetter(i -> i.transform),
                    HeightMap.CODEC
                            .fieldOf("height_map")
                            .forGetter(i -> i.heightMap),
                    BiomeMap.CODEC
                            .fieldOf("surface_biome_map")
                            .forGetter(i -> i.surfaceBiomeMap),
                    BiomeMapLayer.CODEC.listOf()
                            .fieldOf("cave_biomes")
                            .forGetter(i -> i.caveBiomes)
            ).apply(instance, Atlas::new)
    );

    private final ImageTransform transform;
    private final Holder<HeightMap> heightMap;
    private final Holder<BiomeMap> surfaceBiomeMap;
    private final List<BiomeMapLayer> caveBiomes;

    public Atlas(ImageTransform transform, Holder<HeightMap> heightMap, Holder<BiomeMap> surfaceBiomeMap, List<BiomeMapLayer> caveBiomes) {
        this.transform = transform;
        this.heightMap = heightMap;
        this.surfaceBiomeMap = surfaceBiomeMap;
        this.caveBiomes = caveBiomes;
    }

    public BiomeResolver createBiomeResolver(Holder<Biome> defaultBiome, ImageLoader<ColorImageData> loader) {
        if (this.caveBiomes.isEmpty()) {
            return (quartX, _, quartZ) -> this.getSurfaceBiome(quartX, quartZ, defaultBiome, loader);
        } else {
            return (quartX, quartY, quartZ) -> this.getBiomeWithCaves(quartX, quartY, quartZ, defaultBiome, loader);
        }
    }

    @NotNull
    private Holder<Biome> getBiomeWithCaves(int quartX, int quartY, int quartZ, Holder<Biome> defaultBiome, ImageLoader<ColorImageData> loader) {
        Holder<Biome> surfaceBiome = this.getSurfaceBiome(quartX, quartZ, defaultBiome, loader);

        for (BiomeMapLayer layer : this.caveBiomes) {
            if (layer.isInLayer(quartY)) {
                return layer.getBiome(quartX, quartZ, loader, this.transform, surfaceBiome);
            }
        }

        return surfaceBiome;
    }

    @NotNull
    private Holder<Biome> getSurfaceBiome(int quartX, int quartZ, Holder<Biome> defaultBiome, ImageLoader<ColorImageData> loader) {
        return this.surfaceBiomeMap.value().getBiome(quartX, quartZ, loader, this.transform, defaultBiome);
    }
}