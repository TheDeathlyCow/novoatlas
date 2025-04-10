package com.thedeathlycow.novoatlas.neoforge;

import com.thedeathlycow.novoatlas.NovoAtlas;
import com.thedeathlycow.novoatlas.registry.NovoAtlasResourceKeys;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@Mod(NovoAtlas.MOD_ID)
public final class NovoAtlasNeoForge {
    public NovoAtlasNeoForge(IEventBus bus) {
        NovoAtlas.init();
        bus.addListener(NovoAtlasNeoForge::registerDatapackRegistries);
    }

    private static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(NovoAtlasResourceKeys.MAP_INFO, MapInfo.CODEC);
    }
}
