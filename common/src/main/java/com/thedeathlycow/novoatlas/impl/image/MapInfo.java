package com.thedeathlycow.novoatlas.impl.image;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.image.biome.provider.ColorMapBiomeProvider;
import com.thedeathlycow.novoatlas.impl.image.biome.provider.LayeredMapBiomeProvider;
import com.thedeathlycow.novoatlas.impl.registry.MapImageRegistry;
import com.thedeathlycow.novoatlas.impl.registry.NovoAtlasRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.codec.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import java.util.Objects;
import java.util.Optional;

public record MapInfo(
        ResourceKey<HeightMapImage> heightMap,
        Optional<ResourceKey<HeightMapImage>> fluidHeightMap,
        ColorMapBiomeProvider surfaceBiomes,
        Optional<LayeredMapBiomeProvider> caveBiomes,
        int startingY,
        int surfaceRange,
        MapScaleConfig scaling,
        Vector2fc centerOffset,
        ImageWrapping imageWrapping
) {
    public static final Codec<MapInfo> DIRECT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceKey.codec(NovoAtlasRegistries.HEIGHTMAP)
                            .fieldOf("height_map")
                            .forGetter(MapInfo::heightMap),
                    ResourceKey.codec(NovoAtlasRegistries.HEIGHTMAP)
                            .optionalFieldOf("fluid_height_map")
                            .forGetter(MapInfo::fluidHeightMap),
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
                            .forGetter(MapInfo::surfaceRange),
                    MapScaleConfig.CODEC
                            .optionalFieldOf("scaling", MapScaleConfig.DEFAULT)
                            .forGetter(MapInfo::scaling),
                    ExtraCodecs.VECTOR2F
                            .optionalFieldOf("center_offset", new Vector2f(0f, 0f))
                            .forGetter(MapInfo::centerOffset),
                    ImageWrapping.CODEC
                            .optionalFieldOf("image_wrapping", ImageWrapping.CLAMP_TO_EDGE)
                            .forGetter(MapInfo::imageWrapping)
            ).apply(instance, MapInfo::new)
    );

    public static final Codec<Holder<MapInfo>> CODEC = RegistryFileCodec.create(NovoAtlasRegistries.MAP_INFO, DIRECT_CODEC, false);

    public static HeightMapImage lookupHeightmap(ResourceKey<HeightMapImage> map) {
        return Objects.requireNonNull(MapImageRegistry.HEIGHTMAP.getImage(map), "Missing height map image " + map);
    }

    public static BiomeMapImage lookupBiomeMap(ResourceKey<BiomeMapImage> map) {
        return Objects.requireNonNull(MapImageRegistry.BIOME_MAP.getImage(map), "Missing biome map image " + map);
    }

    public HeightMapImage getHeightMap() {
        return lookupHeightmap(this.heightMap);
    }

    public Optional<HeightMapImage> getFluidHeightMap() {
        return this.fluidHeightMap.map(MapInfo::lookupHeightmap);
    }

    public BiomeMapImage getSurfaceBiomeMap() {
        return lookupBiomeMap(this.surfaceBiomes.getMap());
    }

    public BiomeResolver createBiomeResolver(Holder<Biome> defaultBiome) {
        if (this.caveBiomes.isPresent()) {
            return (quartX, quartY, quartZ) -> {
                return this.getBiomeWithCaves(quartX, quartY, quartZ, defaultBiome);
            };
        } else {
            return (quartX, quartY, quartZ) -> {
                return this.getSurfaceBiome(quartX, quartY, quartZ, (_, _, _) -> defaultBiome);
            };
        }
    }

    public int getHeightMapElevation(int x, int z) {
        return this.getHeightMap().sample(x, z, this);
    }

    public double getHeightmapDistanceToEdge(int x, int z) {
        return this.getHeightMap().getDistanceToEdge(x, z, this);
    }

    public double getBiomeMapDistanceToEdge(int x, int z) {
        return this.getSurfaceBiomeMap().getDistanceToEdge(x, z, this);
    }

    public boolean isBlockInsideHeightMap(int x, int z) {
        return this.getHeightMap().isBlockInsideImage(x, z, this);
    }

    public boolean isPointInsideBiomeMap(int x, int z) {
        return this.getSurfaceBiomeMap().isBlockInsideImage(x, z, this);
    }

    public int getFluidHeightMapElevation(int x, int z, int seaLevel) {
        Optional<HeightMapImage> fluidMap = this.getFluidHeightMap();

        if (fluidMap.isPresent()) {
            return fluidMap.orElseThrow().sample(x, z, this);
        } else {
            return seaLevel;
        }
    }

    @NotNull
    public Holder<Biome> getBiomeWithCaves(int x, int y, int z, Holder<Biome> defaultBiome) {
        return getBiomeWithCaves(x, y, z, (_, _, _) -> defaultBiome);
    }

    @NotNull
    public Holder<Biome> getBiomeWithCaves(int x, int y, int z, Delegate outsideBoundDelegate) {
        if (this.caveBiomes.isPresent()) {
            Holder<Biome> caveBiome = this.getCaveBiome(x, y, z, this.caveBiomes.orElseThrow());
            if (caveBiome != null) {
                return caveBiome;
            }
        }

        return this.getSurfaceBiome(x, y, z, outsideBoundDelegate);
    }

    @NotNull
    public Holder<Biome> getSurfaceBiome(int x, int y, int z, Delegate outsideBoundDelegate) {
        Holder<Biome> surfaceBiome = this.surfaceBiomes.getBiome(x, y, z, this);
        return surfaceBiome != null ? surfaceBiome : outsideBoundDelegate.getBiome(x, y, z);
    }

    public MapScaleConfig.HorizontalConfig horizontalScale() {
        return scaling.horizontalScale();
    }

    public float verticalScale() {
        return this.scaling.verticalScale();
    }

    @Nullable
    private Holder<Biome> getCaveBiome(int x, int y, int z, LayeredMapBiomeProvider caveBiomes) {
        int height = this.getHeightMapElevation(x, z);

        if (y <= height - this.surfaceRange) {
            Holder<Biome> caveBiome = caveBiomes.getBiome(x, y, z, this);
            if (caveBiome != null) {
                return caveBiome;
            }
        }

        return null;
    }

    @FunctionalInterface
    public interface Delegate {
        @NotNull Holder<Biome> getBiome(int x, int y, int z);
    }
}