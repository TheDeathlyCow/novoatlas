package com.thedeathlycow.novoatlas.impl.fabric;

import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.gen.biome.BiomeCellColorMapBiomeSource;
import com.thedeathlycow.novoatlas.impl.gen.biome.ColorMapBiomeSource;
import com.thedeathlycow.novoatlas.impl.gen.chunk.ImageMapChunkGenerator;
import com.thedeathlycow.novoatlas.impl.gen.density.HeightmapDensityFunction;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import com.thedeathlycow.novoatlas.impl.image.interpolation.Bicubic;
import com.thedeathlycow.novoatlas.impl.image.interpolation.Bilinear;
import com.thedeathlycow.novoatlas.impl.image.interpolation.Lanczos;
import com.thedeathlycow.novoatlas.impl.image.interpolation.NearestNeighbour;
import com.thedeathlycow.novoatlas.impl.registry.MapImageRegistry;
import com.thedeathlycow.novoatlas.impl.registry.NovoAtlasBuiltinRegistries;
import com.thedeathlycow.novoatlas.impl.registry.NovoAtlasRegistries;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public final class NovoAtlasFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NovoAtlas.init();

        DynamicRegistries.register(NovoAtlasRegistries.MAP_INFO, MapInfo.DIRECT_CODEC);

        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, NovoAtlas.id("image_map"), ImageMapChunkGenerator.CODEC);

        Registry.register(BuiltInRegistries.BIOME_SOURCE, NovoAtlas.id("color_map"), ColorMapBiomeSource.CODEC);
        Registry.register(BuiltInRegistries.BIOME_SOURCE, NovoAtlas.id("biome_cell_color_map"), BiomeCellColorMapBiomeSource.CODEC);

        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, NovoAtlas.id("heightmap"), HeightmapDensityFunction.DATA_CODEC);

        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("nearest_neighbor"), NearestNeighbour.CODEC);
        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bilinear"), Bilinear.CODEC);
        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bicubic"), Bicubic.CODEC);
        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("lanczos"), Lanczos.CODEC);

        addDefaultAlias(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("nearest_neighbor"));
        addDefaultAlias(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bilinear"));
        addDefaultAlias(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bicubic"));
        addDefaultAlias(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("lanczos"));

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new ImageRegistryWrapper<>(MapImageRegistry.HEIGHTMAP)
        );

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new ImageRegistryWrapper<>(MapImageRegistry.BIOME_MAP)
        );

        ModContainer mod = FabricLoader.getInstance().getModContainer(NovoAtlas.MOD_ID).orElseThrow();

        ResourcePackActivationType activation = NovoAtlas.enableExampleDataPacks() ? ResourcePackActivationType.DEFAULT_ENABLED : ResourcePackActivationType.NORMAL;

        ResourceManagerHelper.registerBuiltinResourcePack(NovoAtlas.id("avila-basic-example"), mod, activation);
        ResourceManagerHelper.registerBuiltinResourcePack(NovoAtlas.id("avila-blend-to-random-example"), mod, activation);
        ResourceManagerHelper.registerBuiltinResourcePack(NovoAtlas.id("avila-cave-biome-example"), mod, activation);
        ResourceManagerHelper.registerBuiltinResourcePack(NovoAtlas.id("avila-no-caves-example"), mod, activation);
    }

    private static void addDefaultAlias(Registry<?> registry, ResourceLocation id) {
        registry.addAlias(ResourceLocation.withDefaultNamespace(id.getPath()), id);
    }
}
