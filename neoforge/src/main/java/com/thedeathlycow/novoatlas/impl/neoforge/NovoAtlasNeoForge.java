package com.thedeathlycow.novoatlas.impl.neoforge;

import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.gen.*;
import com.thedeathlycow.novoatlas.impl.registry.ImageManager;
import com.thedeathlycow.novoatlas.impl.registry.NovoAtlasBuiltinRegistries;
import com.thedeathlycow.novoatlas.impl.registry.NovoAtlasRegistries;
import com.thedeathlycow.novoatlas.impl.gen.biome.ColorMapBiomeSource;
import com.thedeathlycow.novoatlas.impl.gen.interpolation.Bicubic;
import com.thedeathlycow.novoatlas.impl.gen.interpolation.Bilinear;
import com.thedeathlycow.novoatlas.impl.gen.interpolation.Lanczos;
import com.thedeathlycow.novoatlas.impl.gen.interpolation.NearestNeighbour;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
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

        NeoForge.EVENT_BUS.addListener(NovoAtlasNeoForge::registerResourceReloader);

        bus.addListener(NovoAtlasNeoForge::registerDatapackRegistries);
        bus.addListener(NovoAtlasNeoForge::register);
        bus.addListener(NovoAtlasNeoForge::addExamplePacks);
    }

    private static void registerRegistries(NewRegistryEvent event) {
        event.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE);
    }

    private static void addExamplePacks(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            PackSource packSource = FMLEnvironment.isProduction()
                    ? PackSource.FEATURE
                    : PackSource.WORLD;

            event.addPackFinders(
                    NovoAtlas.id("resourcepacks/avila-basic-example"),
                    PackType.SERVER_DATA,
                    Component.literal("novoatlas/avila-basic-example"),
                    packSource,
                    false,
                    Pack.Position.TOP
            );

            event.addPackFinders(
                    NovoAtlas.id("resourcepacks/avila-cave-biome-example"),
                    PackType.SERVER_DATA,
                    Component.literal("novoatlas/avila-cave-biome-example"),
                    PackSource.FEATURE,
                    false,
                    Pack.Position.TOP
            );

            event.addPackFinders(
                    NovoAtlas.id("resourcepacks/avila-no-caves-example"),
                    PackType.SERVER_DATA,
                    Component.literal("novoatlas/avila-no-caves-example"),
                    PackSource.FEATURE,
                    false,
                    Pack.Position.TOP
            );
        }
    }

    private static void register(RegisterEvent event) {
        event.register(Registries.CHUNK_GENERATOR, NovoAtlas.id("image_map"), () -> ImageMapChunkGenerator.CODEC);
        event.register(Registries.BIOME_SOURCE, NovoAtlas.id("color_map"), () -> ColorMapBiomeSource.CODEC);
        event.register(Registries.DENSITY_FUNCTION_TYPE, NovoAtlas.id("heightmap"), () -> HeightmapDensityFunction.DATA_CODEC);
        event.register(Registries.DENSITY_FUNCTION_TYPE, NovoAtlas.id("get_height_from_map"), () -> GetHeightFromMapDensityFunction.DATA_CODEC);
        event.register(Registries.DENSITY_FUNCTION_TYPE, NovoAtlas.id("get_preliminary_height_from_map"), () -> GetPreliminaryHeightFromMapDensityFunction.DATA_CODEC);

        if (event.getRegistryKey() == NovoAtlasRegistries.INTERPOLATOR_TYPE) {
            event.register(NovoAtlasRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("nearest_neighbor"), () -> NearestNeighbour.CODEC);
            event.register(NovoAtlasRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bilinear"), () -> Bilinear.CODEC);
            event.register(NovoAtlasRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bicubic"), () -> Bicubic.CODEC);
            event.register(NovoAtlasRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("lanczos"), () -> Lanczos.CODEC);

            addDefaultAlias(event.getRegistry(), NovoAtlas.id("nearest_neighbor"));
            addDefaultAlias(event.getRegistry(), NovoAtlas.id("bilinear"));
            addDefaultAlias(event.getRegistry(), NovoAtlas.id("bicubic"));
            addDefaultAlias(event.getRegistry(), NovoAtlas.id("lanczos"));
        }
    }

    private static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(NovoAtlasRegistries.MAP_INFO, MapInfo.DIRECT_CODEC);
    }

    private static void registerResourceReloader(AddServerReloadListenersEvent event) {
        event.addListener(NovoAtlasRegistries.HEIGHTMAP.identifier(), ImageManager.HEIGHTMAP);
        event.addListener(NovoAtlasRegistries.BIOME_MAP.identifier(), ImageManager.BIOME_MAP);
    }

    private static void addDefaultAlias(Registry<?> registry, Identifier id) {
        registry.addAlias(Identifier.withDefaultNamespace(id.getPath()), id);
    }
}
