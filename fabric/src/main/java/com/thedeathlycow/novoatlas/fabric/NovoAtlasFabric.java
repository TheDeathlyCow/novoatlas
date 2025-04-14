package com.thedeathlycow.novoatlas.fabric;

import com.thedeathlycow.novoatlas.NovoAtlas;
import com.thedeathlycow.novoatlas.registry.NovoAtlasResourceKeys;
import com.thedeathlycow.novoatlas.world.gen.HeightmapDensityFunction;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import com.thedeathlycow.novoatlas.world.gen.NovoAtlasChunkGenerator;
import com.thedeathlycow.novoatlas.world.gen.biome.ColorMapBiomeSource;
import com.thedeathlycow.novoatlas.world.gen.biome.LayeredHeightmapBiomeSource;
import com.thedeathlycow.novoatlas.world.gen.condition.AbovePreliminarySurface;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.PackType;

public final class NovoAtlasFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NovoAtlas.init();

        DynamicRegistries.register(NovoAtlasResourceKeys.MAP_INFO, MapInfo.DIRECT_CODEC);

        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, NovoAtlas.loc("heightmap"), NovoAtlasChunkGenerator.CODEC);
        Registry.register(BuiltInRegistries.MATERIAL_CONDITION, NovoAtlas.loc("above_preliminary_surface"), AbovePreliminarySurface.CODEC.codec());
        Registry.register(BuiltInRegistries.BIOME_SOURCE, NovoAtlas.loc("color_map"), ColorMapBiomeSource.CODEC);
        Registry.register(BuiltInRegistries.BIOME_SOURCE, NovoAtlas.loc("layered_heightmap"), LayeredHeightmapBiomeSource.CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, NovoAtlas.loc("heightmap"), HeightmapDensityFunction.DATA_CODEC);

        ResourceManagerHelper serverManager = ResourceManagerHelper.get(PackType.SERVER_DATA);
        serverManager.registerReloadListener(new MapImageLoader());
    }
}
