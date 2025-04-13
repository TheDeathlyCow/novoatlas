package com.thedeathlycow.novoatlas.neoforge;

import com.thedeathlycow.novoatlas.NovoAtlas;
import com.thedeathlycow.novoatlas.registry.NovoAtlasResourceKeys;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import com.thedeathlycow.novoatlas.world.gen.NovoAtlasChunkGenerator;
import com.thedeathlycow.novoatlas.world.gen.biome.ColorMapBiomeSource;
import com.thedeathlycow.novoatlas.world.gen.biome.LayeredHeightmapBiomeSource;
import com.thedeathlycow.novoatlas.world.gen.condition.AbovePreliminarySurface;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(NovoAtlas.MOD_ID)
public final class NovoAtlasNeoForge {
    public NovoAtlasNeoForge(IEventBus bus) {
        NovoAtlas.init();
        bus.addListener(NovoAtlasNeoForge::registerDatapackRegistries);
        NeoForge.EVENT_BUS.addListener(NovoAtlasNeoForge::registerResourceReloader);
        bus.addListener(NovoAtlasNeoForge::register);
    }

    private static void register(RegisterEvent event) {
        event.register(
                Registries.CHUNK_GENERATOR,
                NovoAtlas.loc("heightmap"),
                () -> NovoAtlasChunkGenerator.CODEC
        );

        event.register(
                Registries.BIOME_SOURCE,
                NovoAtlas.loc("color_map"),
                () -> ColorMapBiomeSource.CODEC
        );

        event.register(
                Registries.BIOME_SOURCE,
                NovoAtlas.loc("layered_heightmap"),
                () -> LayeredHeightmapBiomeSource.CODEC
        );

        event.register(
                Registries.MATERIAL_CONDITION,
                NovoAtlas.loc("above_preliminary_surface"),
                AbovePreliminarySurface.CODEC::codec
        );
    }

    private static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(NovoAtlasResourceKeys.MAP_INFO, MapInfo.DIRECT_CODEC);
    }

    private static void registerResourceReloader(AddServerReloadListenersEvent event) {
        event.addListener(MapImageLoader.ID, new MapImageLoader());
    }
}
