package com.thedeathlycow.novoatlas.impl.neoforge.v1;

import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.v1.gen.biome.BiomeCellColorMapBiomeSource;
import com.thedeathlycow.novoatlas.impl.v1.gen.biome.BoundedMapBiomeSource;
import com.thedeathlycow.novoatlas.impl.v1.gen.biome.ColorMapBiomeSource;
import com.thedeathlycow.novoatlas.impl.v1.gen.chunk.BlendImageToRandomChunkGenerator;
import com.thedeathlycow.novoatlas.impl.v1.gen.chunk.ImageMapChunkGenerator;
import com.thedeathlycow.novoatlas.impl.v1.gen.density.*;
import com.thedeathlycow.novoatlas.impl.v1.image.MapInfo;
import com.thedeathlycow.novoatlas.impl.v1.registry.MapImageRegistry;
import com.thedeathlycow.novoatlas.impl.v1.registry.OldNovoAtlasRegistries;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.HeightMap;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.Bicubic;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.Bilinear;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.Lanczos;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.NearestNeighbour;
import com.thedeathlycow.novoatlas.impl.v2.registry.NovoAtlasBuiltinRegistries;
import com.thedeathlycow.novoatlas.impl.v2.registry.NovoAtlasRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(NovoAtlas.MOD_ID)
public final class NovoAtlasNeoForge {
    public NovoAtlasNeoForge(IEventBus bus) {
        bus.addListener(NovoAtlasNeoForge::registerRegistries);
        NovoAtlas.init();

//        NeoForge.EVENT_BUS.addListener(NovoAtlasNeoForge::registerResourceReloader);
        bus.addListener(NovoAtlasNeoForge::registerDatapackRegistries);
//        bus.addListener(NovoAtlasNeoForge::register);
        bus.addListener(NovoAtlasNeoForge::addExamplePacks);

        NeoForge.EVENT_BUS.addListener(NovoAtlasNeoForge::registerResourceReloaderV1);
        bus.addListener(NovoAtlasNeoForge::registerDatapackRegistriesV1);
        bus.addListener(NovoAtlasNeoForge::registerV1);
    }

    private static void registerRegistries(NewRegistryEvent event) {
        event.register(NovoAtlasBuiltinRegistries.IMAGE_SAMPLER_TYPE);
        event.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE);
    }


    private static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(NovoAtlasRegistries.HEIGHT_MAP, HeightMap.DIRECT_CODEC);
    }

    private static void addExamplePacks(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            PackSource packSource = NovoAtlas.enableExampleDataPacks() ? PackSource.WORLD : PackSource.FEATURE;

            event.addPackFinders(
                    NovoAtlas.id("resourcepacks/avila-basic-example"),
                    PackType.SERVER_DATA,
                    Component.literal("novoatlas/avila-basic-example"),
                    packSource,
                    false,
                    Pack.Position.TOP
            );

            event.addPackFinders(
                    NovoAtlas.id("resourcepacks/avila-blend-to-random-example"),
                    PackType.SERVER_DATA,
                    Component.literal("novoatlas/avila-blend-to-random-example"),
                    packSource,
                    false,
                    Pack.Position.TOP
            );

            event.addPackFinders(
                    NovoAtlas.id("resourcepacks/avila-cave-biome-example"),
                    PackType.SERVER_DATA,
                    Component.literal("novoatlas/avila-cave-biome-example"),
                    packSource,
                    false,
                    Pack.Position.TOP
            );

            event.addPackFinders(
                    NovoAtlas.id("resourcepacks/avila-no-caves-example"),
                    PackType.SERVER_DATA,
                    Component.literal("novoatlas/avila-no-caves-example"),
                    packSource,
                    false,
                    Pack.Position.TOP
            );
        }
    }

    private static void registerV1(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.CHUNK_GENERATOR) {
            event.register(Registries.CHUNK_GENERATOR, NovoAtlas.id("image_map"), () -> ImageMapChunkGenerator.CODEC);
            event.register(Registries.CHUNK_GENERATOR, NovoAtlas.expId("blend_image_map_to_random"), () -> BlendImageToRandomChunkGenerator.CODEC);
        }

        if (event.getRegistryKey() == Registries.BIOME_SOURCE) {
            event.register(Registries.BIOME_SOURCE, NovoAtlas.id("color_map"), () -> ColorMapBiomeSource.CODEC);
            event.register(Registries.BIOME_SOURCE, NovoAtlas.id("biome_cell_color_map"), () -> BiomeCellColorMapBiomeSource.CODEC);
            event.register(Registries.BIOME_SOURCE, NovoAtlas.expId("bounded_biome_cell_color_map"), () -> BoundedMapBiomeSource.CODEC);
        }

        if (event.getRegistryKey() == Registries.DENSITY_FUNCTION_TYPE) {
            event.register(Registries.DENSITY_FUNCTION_TYPE, NovoAtlas.id("heightmap"), () -> HeightmapDensityFunction.CODEC);
            event.register(Registries.DENSITY_FUNCTION_TYPE, NovoAtlas.id("get_height_from_map"), () -> GetHeightFromMapDensityFunction.CODEC);
            event.register(Registries.DENSITY_FUNCTION_TYPE, NovoAtlas.id("get_preliminary_height_from_map"), () -> GetPreliminaryHeightFromMapDensityFunction.CODEC);
            event.register(Registries.DENSITY_FUNCTION_TYPE, NovoAtlas.expId("blend_at_map_border"), () -> BlendAtMapBorder.CODEC);
            event.register(Registries.DENSITY_FUNCTION_TYPE, NovoAtlas.expId("blend_surface_level"), () -> BlendSurfaceLevel.CODEC);
        }

        if (event.getRegistryKey() == OldNovoAtlasRegistries.INTERPOLATOR_TYPE) {
            event.register(OldNovoAtlasRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("nearest_neighbor"), () -> NearestNeighbour.CODEC);
            event.register(OldNovoAtlasRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bilinear"), () -> Bilinear.CODEC);
            event.register(OldNovoAtlasRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bicubic"), () -> Bicubic.CODEC);
            event.register(OldNovoAtlasRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("lanczos"), () -> Lanczos.CODEC);

            addDefaultAlias(event.getRegistry(), NovoAtlas.id("nearest_neighbor"));
            addDefaultAlias(event.getRegistry(), NovoAtlas.id("bilinear"));
            addDefaultAlias(event.getRegistry(), NovoAtlas.id("bicubic"));
            addDefaultAlias(event.getRegistry(), NovoAtlas.id("lanczos"));
        }
    }

    private static void registerDatapackRegistriesV1(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(OldNovoAtlasRegistries.MAP_INFO, MapInfo.DIRECT_CODEC);
    }

    private static void registerResourceReloaderV1(AddServerReloadListenersEvent event) {
        event.addListener(OldNovoAtlasRegistries.HEIGHTMAP.identifier(), MapImageRegistry.HEIGHTMAP);
        event.addListener(OldNovoAtlasRegistries.BIOME_MAP.identifier(), MapImageRegistry.BIOME_MAP);
    }

    private static void addDefaultAlias(Registry<?> registry, Identifier id) {
        registry.addAlias(Identifier.withDefaultNamespace(id.getPath()), id);
    }
}
