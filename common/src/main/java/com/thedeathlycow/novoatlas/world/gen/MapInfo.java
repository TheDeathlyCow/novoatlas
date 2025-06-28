package com.thedeathlycow.novoatlas.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.registry.ImageManager;
import com.thedeathlycow.novoatlas.registry.NovoAtlasResourceKeys;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;

import java.util.Objects;

public record MapInfo(
        ResourceKey<MapImage> heightMap,
        ResourceKey<MapImage> biomeMap,
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
                    Codec.INT
                            .fieldOf("starting_y")
                            .forGetter(MapInfo::startingY)
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

    public int getBiomeColor(int x, int z, int fallback) {
        return lookupBiomeMap(this.biomeMap).sample(x, z, this, fallback);
    }

    public float horizontalScale() {
        return 1.0f;
    }

    public float verticalScale() {
        return 1.0f;
    }
}