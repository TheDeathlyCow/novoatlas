package com.thedeathlycow.novoatlas.impl.registry;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.image.MapImage;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import com.thedeathlycow.novoatlas.impl.image.interpolation.Interpolator;
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