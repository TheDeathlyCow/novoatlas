package com.thedeathlycow.novoatlas.impl.fabric;

import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.gen.*;
import com.thedeathlycow.novoatlas.impl.platform.Services;
import com.thedeathlycow.novoatlas.impl.registry.ImageManager;
import com.thedeathlycow.novoatlas.impl.registry.NovoAtlasBuiltinRegistries;
import com.thedeathlycow.novoatlas.impl.registry.NovoAtlasRegistries;
import com.thedeathlycow.novoatlas.impl.gen.biome.ColorMapBiomeSource;
import com.thedeathlycow.novoatlas.impl.gen.interpolation.Bicubic;
import com.thedeathlycow.novoatlas.impl.gen.interpolation.Bilinear;
import com.thedeathlycow.novoatlas.impl.gen.interpolation.Lanczos;
import com.thedeathlycow.novoatlas.impl.gen.interpolation.NearestNeighbour;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class NovoAtlasFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NovoAtlas.init();

        DynamicRegistries.register(NovoAtlasRegistries.MAP_INFO, MapInfo.DIRECT_CODEC);
        DynamicRegistrySetupCallback.EVENT.register(view -> {
            view.registerEntryAdded(
                    NovoAtlasRegistries.MAP_INFO,
                    (_, id, object) -> MapInfo.onObjectRegistered(id, object));
        });

        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, NovoAtlas.id("image_map"), ImageMapChunkGenerator.CODEC);
        Registry.register(BuiltInRegistries.BIOME_SOURCE, NovoAtlas.id("color_map"), ColorMapBiomeSource.CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, NovoAtlas.id("heightmap"), HeightmapDensityFunction.DATA_CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, NovoAtlas.id("get_height_from_map"), GetHeightFromMapDensityFunction.DATA_CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, NovoAtlas.id("get_preliminary_height_from_map"), GetPreliminaryHeightFromMapDensityFunction.DATA_CODEC);
        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("nearest_neighbor"), NearestNeighbour.CODEC);
        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bilinear"), Bilinear.CODEC);
        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bicubic"), Bicubic.CODEC);
        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("lanczos"), Lanczos.CODEC);

        addDefaultAlias(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("nearest_neighbor"));
        addDefaultAlias(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bilinear"));
        addDefaultAlias(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bicubic"));
        addDefaultAlias(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("lanczos"));

        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(NovoAtlasRegistries.HEIGHTMAP.identifier(), ImageManager.HEIGHTMAP);
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(NovoAtlasRegistries.BIOME_MAP.identifier(), ImageManager.BIOME_MAP);

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

    private static void addDefaultAlias(Registry<?> registry, Identifier id) {
        registry.addAlias(Identifier.withDefaultNamespace(id.getPath()), id);
    }
}
