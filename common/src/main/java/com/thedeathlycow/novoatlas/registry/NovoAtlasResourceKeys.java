package com.thedeathlycow.novoatlas.registry;

import com.thedeathlycow.novoatlas.NovoAtlas;
import com.thedeathlycow.novoatlas.world.gen.MapImage;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class NovoAtlasResourceKeys {
    public static final ResourceKey<Registry<MapInfo>> MAP_INFO = ResourceKey.createRegistryKey(
            NovoAtlas.id("map_info")
    );

    public static final ResourceKey<Registry<MapImage>> HEIGHTMAP = ResourceKey.createRegistryKey(
            NovoAtlas.id("heightmap")
    );

    public static final ResourceKey<Registry<MapImage>> BIOME_MAP = ResourceKey.createRegistryKey(
            NovoAtlas.id("biome_map")
    );

    private NovoAtlasResourceKeys() {
    }
}