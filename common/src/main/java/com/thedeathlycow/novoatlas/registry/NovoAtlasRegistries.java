package com.thedeathlycow.novoatlas.registry;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.NovoAtlas;
import com.thedeathlycow.novoatlas.world.gen.MapImage;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import com.thedeathlycow.novoatlas.world.gen.interpolation.Interpolator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class NovoAtlasRegistries {
    public static final ResourceKey<Registry<MapInfo>> MAP_INFO = ResourceKey.createRegistryKey(
            NovoAtlas.id("map_info")
    );

    public static final ResourceKey<Registry<MapImage>> HEIGHTMAP = ResourceKey.createRegistryKey(
            NovoAtlas.id("heightmap")
    );

    public static final ResourceKey<Registry<MapImage>> BIOME_MAP = ResourceKey.createRegistryKey(
            NovoAtlas.id("biome_map")
    );

    public static final ResourceKey<Registry<MapCodec<? extends Interpolator>>> INTERPOLATOR_TYPE = ResourceKey.createRegistryKey(
            NovoAtlas.id("interpolator_type")
    );

    private NovoAtlasRegistries() {
    }
}