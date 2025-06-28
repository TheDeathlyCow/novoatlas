package com.thedeathlycow.novoatlas.registry;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.NovoAtlas;
import com.thedeathlycow.novoatlas.world.gen.MapImage;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import com.thedeathlycow.novoatlas.world.gen.biome.provider.BiomeMapProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class NovoAtlasResourceKeys {
    public static final ResourceKey<Registry<MapInfo>> MAP_INFO = ResourceKey.createRegistryKey(
            NovoAtlas.loc("map_info")
    );

    public static final ResourceKey<Registry<MapImage>> HEIGHTMAP = ResourceKey.createRegistryKey(
            NovoAtlas.loc("heightmap")
    );

    public static final ResourceKey<Registry<MapImage>> BIOME_MAP = ResourceKey.createRegistryKey(
            NovoAtlas.loc("biome_map")
    );

    public static final ResourceKey<Registry<MapCodec<? extends BiomeMapProvider>>> BIOME_MAP_PROVIDER = ResourceKey.createRegistryKey(
            NovoAtlas.loc("biome_map_provider")
    );

    private NovoAtlasResourceKeys() {
    }
}