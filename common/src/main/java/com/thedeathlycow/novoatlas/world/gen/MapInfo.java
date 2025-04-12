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
        ResourceLocation heightMap,
        float horizontalScale,
        float verticalScale,
        int startingY
) {
    public static final Codec<MapInfo> DIRECT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC
                            .fieldOf("height_map")
                            .forGetter(MapInfo::heightMap),
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
        ResourceKey<MapImage> key = ResourceKey.create(NovoAtlasResourceKeys.HEIGHTMAP, this.heightMap);

        return Objects.requireNonNull(ImageManager.HEIGHTMAP.getImage(key), "Missing height map image " + key);
    }

    public double getHeightMapElevation(int x, int z) {
        return this.lookupHeightmap().getElevation(x, z, this);
    }
}