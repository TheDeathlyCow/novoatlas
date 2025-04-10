package com.thedeathlycow.novoatlas.fabric;

import com.thedeathlycow.novoatlas.registry.NovoAtlasResourceKeys;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import net.fabricmc.api.ModInitializer;

import com.thedeathlycow.novoatlas.NovoAtlas;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public final class NovoAtlasFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NovoAtlas.init();

        DynamicRegistries.register(NovoAtlasResourceKeys.MAP_INFO, MapInfo.CODEC);

        ResourceManagerHelper serverManager = ResourceManagerHelper.get(PackType.SERVER_DATA);
        serverManager.registerReloadListener(new MapImageLoader());
    }
}
