package com.thedeathlycow.novoatlas.impl.image;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.registry.MapImageRegistry;
import com.thedeathlycow.novoatlas.impl.registry.NovoAtlasRegistries;
import com.thedeathlycow.novoatlas.impl.image.biome.provider.ColorMapBiomeProvider;
import com.thedeathlycow.novoatlas.impl.image.biome.provider.LayeredMapBiomeProvider;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.util.List;
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
        Vector2f centerOffset,
        ImageWrapping imageWrapping
) {
    private static final Codec<Vector2f> VECTOR2F_CODEC = Codec.FLOAT.listOf()
            .comapFlatMap(list ->
                    Util.fixedSize(list, 2).map(
                            floats -> new Vector2f(floats.get(0), floats.get(1))
                    ),
                    vector2f -> List.of(vector2f.x(), vector2f.y())
            );

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
                    VECTOR2F_CODEC
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

    public int getHeightMapElevation(int x, int z) {
        return lookupHeightmap(this.heightMap).sample(x, z, this);
    }

    public float getDistanceToEdge(int x, int z) {
        return lookupHeightmap(this.heightMap).getDistanceToEdge(x, z, this);
    }

    public boolean isBlockInsideHeightMap(int x, int z) {
        return lookupHeightmap(this.heightMap).isBlockInsideImage(x, z, this);
    }

    public boolean isPointInsideBiomeMap(int x, int z) {
        return lookupBiomeMap(this.surfaceBiomes.getMap()).isBlockInsideImage(x, z, this);
    }

    public int getFluidHeightMapElevation(int x, int z, int seaLevel) {
        if (this.fluidHeightMap.isPresent()) {
            return lookupHeightmap(this.fluidHeightMap.orElseThrow()).sample(x, z, this);
        } else {
            return seaLevel;
        }
    }

    public BiomeResolver createBiomeResolver(Holder<Biome> defaultBiome) {
        return this.createBiomeResolver(QuartPos::toBlock, defaultBiome);
    }

    public BiomeResolver createBiomeResolver(QuartPosConverter converter, Holder<Biome> defaultBiome) {
        BiomeResolver defaultBiomeResolver = (ix, iy, iz, is) -> defaultBiome;

        if (this.caveBiomes.isPresent()) {
            return (quartX, quartY, quartZ, sampler) -> this.getBiome(quartX, quartY, quartZ, sampler, converter, defaultBiomeResolver);
        } else {
            return (quartX, quartY, quartZ, sampler) -> this.getSurfaceBiome(quartX, quartY, quartZ, sampler, converter, defaultBiomeResolver);
        }
    }

    @NotNull
    public Holder<Biome> getBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler, QuartPosConverter converter, BiomeResolver outsideBoundDelegate) {
        if (this.caveBiomes.isPresent()) {
            Holder<Biome> caveBiome = this.getCaveBiome(quartX, quartY, quartZ, converter, this.caveBiomes.orElseThrow());
            if (caveBiome != null) {
                return caveBiome;
            }
        }

        return this.getSurfaceBiome(quartX, quartY, quartZ, sampler, converter, outsideBoundDelegate);
    }

    @NotNull
    public Holder<Biome> getSurfaceBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler, QuartPosConverter converter, BiomeResolver outsideBoundDelegate) {
        Holder<Biome> surfaceBiome = this.surfaceBiomes.getBiome(quartX, converter.toBlockPos(quartY), quartZ, this);
        return surfaceBiome != null ? surfaceBiome : outsideBoundDelegate.getNoiseBiome(quartX, quartY, quartZ, sampler);
    }

    public MapScaleConfig.HorizontalConfig horizontalScale() {
        return scaling.horizontalScale();
    }

    public float verticalScale() {
        return this.scaling.verticalScale();
    }

    @Nullable
    private Holder<Biome> getCaveBiome(int quartX, int quartY, int quartZ, QuartPosConverter converter, LayeredMapBiomeProvider caveBiomes) {
        int height = this.getHeightMapElevation(converter.toBlockPos(quartX), converter.toBlockPos(quartZ));
        int blockY = converter.toBlockPos(quartY);

        if (blockY <= height - this.surfaceRange) {
            Holder<Biome> caveBiome = caveBiomes.getBiome(quartX, blockY, quartZ, this);
            if (caveBiome != null) {
                return caveBiome;
            }
        }

        return null;
    }

    @FunctionalInterface
    public interface QuartPosConverter {
        int toBlockPos(int quart);
    }
}