package com.thedeathlycow.novoatlas.impl.fabric;

import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.registry.NovoAtlasResourceKeys;
import com.thedeathlycow.novoatlas.impl.gen.chunk.ImageMapChunkGenerator;
import com.thedeathlycow.novoatlas.impl.gen.density.HeightmapDensityFunction;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import com.thedeathlycow.novoatlas.impl.gen.biome.ColorMapBiomeSource;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.PackType;

public final class NovoAtlasFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NovoAtlas.init();

        DynamicRegistries.register(NovoAtlasResourceKeys.MAP_INFO, MapInfo.DIRECT_CODEC);

        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, NovoAtlas.loc("image_map"), ImageMapChunkGenerator.CODEC);
        Registry.register(BuiltInRegistries.BIOME_SOURCE, NovoAtlas.loc("color_map"), ColorMapBiomeSource.CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, NovoAtlas.loc("heightmap"), HeightmapDensityFunction.DATA_CODEC);

        ResourceManagerHelper serverManager = ResourceManagerHelper.get(PackType.SERVER_DATA);
        serverManager.registerReloadListener(new MapImageLoader());

        ModContainer mod = FabricLoader.getInstance().getModContainer(NovoAtlas.MOD_ID).orElseThrow();

        ResourceManagerHelper.registerBuiltinResourcePack(
                NovoAtlas.loc("avila-basic-example"),
                mod,
                ResourcePackActivationType.NORMAL
        );

        ResourceManagerHelper.registerBuiltinResourcePack(
                NovoAtlas.loc("avila-cave-biome-example"),
                mod,
                ResourcePackActivationType.NORMAL
        );

        ResourceManagerHelper.registerBuiltinResourcePack(
                NovoAtlas.loc("avila-no-caves-example"),
                mod,
                ResourcePackActivationType.NORMAL
        );
    }
}
