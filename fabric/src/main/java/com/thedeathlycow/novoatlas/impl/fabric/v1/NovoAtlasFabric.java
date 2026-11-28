package com.thedeathlycow.novoatlas.impl.fabric.v1;

import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.v1.gen.biome.BiomeCellColorMapBiomeSource;
import com.thedeathlycow.novoatlas.impl.v1.gen.biome.BoundedMapBiomeSource;
import com.thedeathlycow.novoatlas.impl.v1.gen.biome.ColorMapBiomeSource;
import com.thedeathlycow.novoatlas.impl.v1.gen.chunk.BlendImageToRandomChunkGenerator;
import com.thedeathlycow.novoatlas.impl.v1.gen.chunk.ImageMapChunkGenerator;
import com.thedeathlycow.novoatlas.impl.v1.gen.density.*;
import com.thedeathlycow.novoatlas.impl.v1.image.MapInfo;
import com.thedeathlycow.novoatlas.impl.v1.image.interpolation.Bicubic;
import com.thedeathlycow.novoatlas.impl.v1.image.interpolation.Bilinear;
import com.thedeathlycow.novoatlas.impl.v1.image.interpolation.Lanczos;
import com.thedeathlycow.novoatlas.impl.v1.image.interpolation.NearestNeighbour;
import com.thedeathlycow.novoatlas.impl.v1.registry.MapImageRegistry;
import com.thedeathlycow.novoatlas.impl.v1.registry.NovoAtlasBuiltinRegistries;
import com.thedeathlycow.novoatlas.impl.v1.registry.NovoAtlasRegistries;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public final class NovoAtlasFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DynamicRegistries.register(NovoAtlasRegistries.MAP_INFO, MapInfo.DIRECT_CODEC);

        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, NovoAtlas.id("image_map"), ImageMapChunkGenerator.CODEC);
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, NovoAtlas.expId("blend_image_map_to_random"), BlendImageToRandomChunkGenerator.CODEC);

        Registry.register(BuiltInRegistries.BIOME_SOURCE, NovoAtlas.id("color_map"), ColorMapBiomeSource.CODEC);
        Registry.register(BuiltInRegistries.BIOME_SOURCE, NovoAtlas.id("biome_cell_color_map"), BiomeCellColorMapBiomeSource.CODEC);
        Registry.register(BuiltInRegistries.BIOME_SOURCE, NovoAtlas.expId("bounded_biome_cell_color_map"), BoundedMapBiomeSource.CODEC);

        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, NovoAtlas.id("heightmap"), HeightmapDensityFunction.CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, NovoAtlas.id("get_height_from_map"), GetHeightFromMapDensityFunction.CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, NovoAtlas.id("get_preliminary_height_from_map"), GetPreliminaryHeightFromMapDensityFunction.CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, NovoAtlas.expId("blend_at_map_border"), BlendAtMapBorder.CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, NovoAtlas.expId("blend_surface_level"), BlendSurfaceLevel.CODEC);

        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("nearest_neighbor"), NearestNeighbour.CODEC);
        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bilinear"), Bilinear.CODEC);
        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bicubic"), Bicubic.CODEC);
        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("lanczos"), Lanczos.CODEC);

        addDefaultAlias(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("nearest_neighbor"));
        addDefaultAlias(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bilinear"));
        addDefaultAlias(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bicubic"));
        addDefaultAlias(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("lanczos"));

        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(NovoAtlasRegistries.HEIGHTMAP.identifier(), MapImageRegistry.HEIGHTMAP);
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(NovoAtlasRegistries.BIOME_MAP.identifier(), MapImageRegistry.BIOME_MAP);
    }

    private static void addDefaultAlias(Registry<?> registry, Identifier id) {
        registry.addAlias(Identifier.withDefaultNamespace(id.getPath()), id);
    }
}
