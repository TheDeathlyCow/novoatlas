package com.thedeathlycow.novoatlas.impl.registry;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.image.*;
import com.thedeathlycow.novoatlas.impl.image.interpolation.Interpolator;
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

    public static final ResourceKey<Registry<MapCodec<? extends EdgeHandling>>> EDGE_HANDLING = ResourceKey.createRegistryKey(
            NovoAtlas.id("edge_handler")
    );

    private NovoAtlasRegistries() {
    }
}