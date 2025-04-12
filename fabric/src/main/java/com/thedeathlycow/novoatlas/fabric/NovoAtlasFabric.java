package com.thedeathlycow.novoatlas.fabric;

import com.thedeathlycow.novoatlas.registry.NovoAtlasResourceKeys;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import com.thedeathlycow.novoatlas.world.gen.NovoAtlasChunkGenerator;
import com.thedeathlycow.novoatlas.world.gen.condition.AbovePreliminarySurface;
import net.fabricmc.api.ModInitializer;

import com.thedeathlycow.novoatlas.NovoAtlas;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Items;

public final class NovoAtlasFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NovoAtlas.init();

        DynamicRegistries.register(NovoAtlasResourceKeys.MAP_INFO, MapInfo.DIRECT_CODEC);

        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, NovoAtlas.loc("prescribed"), NovoAtlasChunkGenerator.CODEC);
        Registry.register(BuiltInRegistries.MATERIAL_CONDITION, NovoAtlas.loc("above_preliminary_surface"), AbovePreliminarySurface.CODEC.codec());

        ResourceManagerHelper serverManager = ResourceManagerHelper.get(PackType.SERVER_DATA);
        serverManager.registerReloadListener(new MapImageLoader());
    }
}
