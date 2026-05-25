package com.thedeathlycow.novoatlas.fabric;

import com.thedeathlycow.novoatlas.NovoAtlas;
import com.thedeathlycow.novoatlas.platform.Services;
import com.thedeathlycow.novoatlas.registry.ImageManager;
import com.thedeathlycow.novoatlas.registry.NovoAtlasResourceKeys;
import com.thedeathlycow.novoatlas.world.gen.GetHeightFromMapDensityFunction;
import com.thedeathlycow.novoatlas.world.gen.ImageMapChunkGenerator;
import com.thedeathlycow.novoatlas.world.gen.HeightmapDensityFunction;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import com.thedeathlycow.novoatlas.world.gen.biome.ColorMapBiomeSource;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
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

        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, NovoAtlas.id("image_map"), ImageMapChunkGenerator.CODEC);
        Registry.register(BuiltInRegistries.BIOME_SOURCE, NovoAtlas.id("color_map"), ColorMapBiomeSource.CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, NovoAtlas.id("heightmap"), HeightmapDensityFunction.DATA_CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, NovoAtlas.id("get_height_from_map"), GetHeightFromMapDensityFunction.DATA_CODEC);

        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(NovoAtlasResourceKeys.HEIGHTMAP.identifier(), ImageManager.HEIGHTMAP);
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(NovoAtlasResourceKeys.BIOME_MAP.identifier(), ImageManager.BIOME_MAP);

        ModContainer mod = FabricLoader.getInstance().getModContainer(NovoAtlas.MOD_ID).orElseThrow();

        ResourceLoader.registerBuiltinPack(
                NovoAtlas.id("avila-basic-example"),
                mod,
                Services.PLATFORM.isDevelopmentEnvironment()
                        ? PackActivationType.DEFAULT_ENABLED
                        : PackActivationType.NORMAL
        );

        ResourceLoader.registerBuiltinPack(
                NovoAtlas.id("avila-cave-biome-example"),
                mod,
                PackActivationType.NORMAL
        );

        ResourceLoader.registerBuiltinPack(
                NovoAtlas.id("avila-no-caves-example"),
                mod,
                PackActivationType.NORMAL
        );
    }
}
