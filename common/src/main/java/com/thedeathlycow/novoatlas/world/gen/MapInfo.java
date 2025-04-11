package com.thedeathlycow.novoatlas.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.registry.NovoAtlasResourceKeys;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;

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
}