package com.thedeathlycow.novoatlas.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.registry.ImageManager;
import com.thedeathlycow.novoatlas.registry.NovoAtlasResourceKeys;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record MapInfo(
        ResourceKey<MapImage> heightMap,
        ResourceKey<MapImage> biomeMap,
        float horizontalScale,
        float verticalScale,
        int startingY
) {
    public static final Codec<MapInfo> DIRECT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceKey.codec(NovoAtlasResourceKeys.HEIGHTMAP)
                            .fieldOf("height_map")
                            .forGetter(MapInfo::heightMap),
                    ResourceKey.codec(NovoAtlasResourceKeys.BIOME_MAP)
                            .fieldOf("biome_map")
                            .forGetter(MapInfo::biomeMap),
                    Codec.FLOAT
                            .optionalFieldOf("horizontal_scale", 1f)
                            .forGetter(MapInfo::horizontalScale),
                    Codec.FLOAT
                            .optionalFieldOf("vertical_scale", 1f)
                            .forGetter(MapInfo::verticalScale),
                    Codec.INT
                            .fieldOf("starting_y")
                            .forGetter(MapInfo::startingY)
            ).apply(instance, MapInfo::new)
    );

    public static final Codec<Holder<MapInfo>> CODEC = RegistryFileCodec.create(NovoAtlasResourceKeys.MAP_INFO, DIRECT_CODEC);

    public MapImage lookupHeightmap() {
        return Objects.requireNonNull(ImageManager.HEIGHTMAP.getImage(this.heightMap), "Missing height map image " + this.heightMap);
    }

    public MapImage lookupBiomeMap() {
        return Objects.requireNonNull(ImageManager.BIOME_MAP.getImage(this.biomeMap), "Missing biome map image " + this.biomeMap);
    }

    public double getHeightMapElevation(int x, int z, int fallback) {
        return this.lookupHeightmap().getElevation(x, z, this, fallback);
    }

    public double getHeightMapElevation(int x, int z) {
        return this.lookupHeightmap().getElevation(x, z, this);
    }
}