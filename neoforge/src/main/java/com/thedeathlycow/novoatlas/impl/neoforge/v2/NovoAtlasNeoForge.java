package com.thedeathlycow.novoatlas.impl.neoforge.v2;

import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.BiomeMap;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.HeightMap;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.biome.ColorMappingData;
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

        NeoForge.EVENT_BUS.addListener(NovoAtlasNeoForge::registerResourceReloader);
        bus.addListener(NovoAtlasNeoForge::registerDatapackRegistries);
        bus.addListener(NovoAtlasNeoForge::register);
        bus.addListener(NovoAtlasNeoForge::addExamplePacks);
    }

    private static void addDefaultAlias(Registry<?> registry, Identifier id) {
        registry.addAlias(Identifier.withDefaultNamespace(id.getPath()), id);
    }

    private static void registerRegistries(NewRegistryEvent event) {
        event.register(NovoAtlasBuiltinRegistries.IMAGE_SAMPLER_TYPE);
        event.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE);
    }

    private static void registerResourceReloader(AddServerReloadListenersEvent event) {
    }

    private static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(NovoAtlasRegistries.HEIGHT_MAP, HeightMap.DIRECT_CODEC);
        event.dataPackRegistry(NovoAtlasRegistries.BIOME_MAP, BiomeMap.DIRECT_CODEC);
        event.dataPackRegistry(NovoAtlasRegistries.COLOR_MAPPING, ColorMappingData.DIRECT_CODEC);
    }

    private static void register(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.CHUNK_GENERATOR) {
        }

        if (event.getRegistryKey() == Registries.BIOME_SOURCE) {
        }

        if (event.getRegistryKey() == Registries.DENSITY_FUNCTION_TYPE) {
        }

        if (event.getRegistryKey() == NovoAtlasRegistries.INTERPOLATOR_TYPE) {
            event.register(NovoAtlasRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("nearest_neighbor"), () -> NearestNeighbour.CODEC);
            event.register(NovoAtlasRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bilinear"), () -> Bilinear.CODEC);
            event.register(NovoAtlasRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bicubic"), () -> Bicubic.CODEC);
            event.register(NovoAtlasRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("lanczos"), () -> Lanczos.CODEC);
        }
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
}
