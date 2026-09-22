package com.thedeathlycow.novoatlas.impl.fabric;

import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.registry.ImageManager;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class MapImageLoader implements SimpleSynchronousResourceReloadListener {
    public static final ResourceLocation ID = NovoAtlas.loc("map_image");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        ImageManager.HEIGHTMAP.reload(resourceManager);
        ImageManager.BIOME_MAP.reload(resourceManager);
    }
}