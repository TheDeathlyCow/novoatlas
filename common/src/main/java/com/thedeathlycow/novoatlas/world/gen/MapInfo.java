package com.thedeathlycow.novoatlas.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.registry.ImageManager;
import com.thedeathlycow.novoatlas.registry.NovoAtlasResourceKeys;
import com.thedeathlycow.novoatlas.world.gen.biome.provider.ColorMapBiomeProvider;
import com.thedeathlycow.novoatlas.world.gen.biome.provider.LayeredMapBiomeProvider;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public record MapInfo(
        ResourceKey<MapImage> heightMap,
        ColorMapBiomeProvider surfaceBiomes,
        Optional<LayeredMapBiomeProvider> caveBiomes,
        int startingY,
        int surfaceRange
) {
    public static final Codec<MapInfo> DIRECT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceKey.codec(NovoAtlasResourceKeys.HEIGHTMAP)
                            .fieldOf("height_map")
                            .forGetter(MapInfo::heightMap),
                    ColorMapBiomeProvider.CODEC.codec()
                            .fieldOf("surface_biomes")
                            .forGetter(MapInfo::surfaceBiomes),
                    LayeredMapBiomeProvider.CODEC.codec()
                            .optionalFieldOf("cave_biomes")
                            .forGetter(MapInfo::caveBiomes),
                    Codec.INT
                            .fieldOf("starting_y")
                            .forGetter(MapInfo::startingY),
                    ExtraCodecs.POSITIVE_INT
                            .optionalFieldOf("surface_range", 16)
                            .forGetter(MapInfo::surfaceRange)
            ).apply(instance, MapInfo::new)
    );

    public static final Codec<Holder<MapInfo>> CODEC = RegistryFileCodec.create(NovoAtlasResourceKeys.MAP_INFO, DIRECT_CODEC);

    public static MapImage lookupHeightmap(ResourceKey<MapImage> map) {
        return Objects.requireNonNull(ImageManager.HEIGHTMAP.getImage(map), "Missing height map image " + map);
    }

    public static MapImage lookupBiomeMap(ResourceKey<MapImage> map) {
        return Objects.requireNonNull(ImageManager.BIOME_MAP.getImage(map), "Missing biome map image " + map);
    }

    public int getHeightMapElevation(int x, int z, int fallback) {
        return lookupHeightmap(this.heightMap).sample(x, z, this, fallback);
    }

    public int getHeightMapElevation(int x, int z) {
        return lookupHeightmap(this.heightMap).sample(x, z, this);
    }

    @NotNull
    public Holder<Biome> getBiome(int x, int y, int z, @NotNull Holder<Biome> defaultBiome) {
        if (this.caveBiomes.isPresent()) {
            Holder<Biome> caveBiome = this.getCaveBiome(x, y, z, this.caveBiomes.orElseThrow());
            if (caveBiome != null) {
                return caveBiome;
            }
        }

        Holder<Biome> surfaceBiome = this.surfaceBiomes.getBiome(x, y, z, this);
        return surfaceBiome != null ? surfaceBiome : defaultBiome;
    }

    public float horizontalScale() {
        return 1.0f;
    }

    public float verticalScale() {
        return 1.0f;
    }

    @Nullable
    private Holder<Biome> getCaveBiome(int x, int y, int z, LayeredMapBiomeProvider caveBiomes) {
        int height = this.getHeightMapElevation(x, z, Integer.MIN_VALUE);

        if (height == Integer.MIN_VALUE) {
            return null;
        }

        if (y <= height - this.surfaceRange) {
            Holder<Biome> caveBiome = caveBiomes.getBiome(x, y, z, this);
            if (caveBiome != null) {
                return caveBiome;
            }
        }

        return null;
    }
}