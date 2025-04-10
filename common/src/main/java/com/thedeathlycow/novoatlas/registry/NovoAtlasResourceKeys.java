package com.thedeathlycow.novoatlas.registry;

import com.thedeathlycow.novoatlas.NovoAtlas;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class NovoAtlasResourceKeys {
    public static final ResourceKey<Registry<MapInfo>> MAP_INFO = ResourceKey.createRegistryKey(
            NovoAtlas.loc("map_info")
    );

    private NovoAtlasResourceKeys() {

    }
}