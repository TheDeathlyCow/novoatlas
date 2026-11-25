package com.thedeathlycow.novoatlas.impl.v1.registry;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.v1.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.v1.image.BiomeMapImage;
import com.thedeathlycow.novoatlas.impl.v1.image.HeightMapImage;
import com.thedeathlycow.novoatlas.impl.v1.image.MapInfo;
import com.thedeathlycow.novoatlas.impl.v1.image.interpolation.Interpolator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class NovoAtlasRegistries {
    public static final ResourceKey<Registry<MapInfo>> MAP_INFO = ResourceKey.createRegistryKey(
            NovoAtlas.id("map_info")
    );

    public static final ResourceKey<Registry<HeightMapImage>> HEIGHTMAP = ResourceKey.createRegistryKey(
            NovoAtlas.id("heightmap")
    );

    public static final ResourceKey<Registry<BiomeMapImage>> BIOME_MAP = ResourceKey.createRegistryKey(
            NovoAtlas.id("biome_map")
    );

    public static final ResourceKey<Registry<MapCodec<? extends Interpolator>>> INTERPOLATOR_TYPE = ResourceKey.createRegistryKey(
            NovoAtlas.id("interpolator_type")
    );

    private NovoAtlasRegistries() {
    }
}