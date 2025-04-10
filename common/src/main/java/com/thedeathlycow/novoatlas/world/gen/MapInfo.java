package com.thedeathlycow.novoatlas.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

public record MapInfo(
        ResourceLocation heightMap,
        float horizontalScale,
        float verticalScale,
        int startingY
) {
    public static final Codec<MapInfo> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC
                            .fieldOf("height_map")
                            .forGetter(MapInfo::heightMap),
                    Codec.FLOAT
                            .fieldOf("horizontal_scale")
                            .forGetter(MapInfo::horizontalScale),
                    Codec.FLOAT
                            .fieldOf("vertical_scale")
                            .forGetter(MapInfo::verticalScale),
                    Codec.INT
                            .fieldOf("starting_y")
                            .forGetter(MapInfo::startingY)
            ).apply(instance, MapInfo::new)
    );
}