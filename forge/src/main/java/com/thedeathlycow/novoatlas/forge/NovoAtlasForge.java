package com.thedeathlycow.novoatlas.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.NovoAtlas;
import com.thedeathlycow.novoatlas.registry.NovoAtlasResourceKeys;
import com.thedeathlycow.novoatlas.world.gen.HeightmapDensityFunction;
import com.thedeathlycow.novoatlas.world.gen.ImageMapChunkGenerator;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import com.thedeathlycow.novoatlas.world.gen.biome.ColorMapBiomeSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.DeferredRegister;

@Mod(NovoAtlas.MOD_ID)
public final class NovoAtlasForge {
    private static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(Registries.CHUNK_GENERATOR, NovoAtlas.MOD_ID);
    private static final DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCES = DeferredRegister.create(Registries.BIOME_SOURCE, NovoAtlas.MOD_ID);
    private static final DeferredRegister<Codec<? extends DensityFunction>> DENSITY_FUNCTIONS = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, NovoAtlas.MOD_ID);

    public NovoAtlasForge() {
        NovoAtlas.init();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        CHUNK_GENERATORS.register(bus);
        BIOME_SOURCES.register(bus);
        DENSITY_FUNCTIONS.register(bus);

        MinecraftForge.EVENT_BUS.addListener(NovoAtlasForge::registerResourceReloader);

        bus.addListener(NovoAtlasForge::registerDatapackRegistries);
        bus.addListener(NovoAtlasForge::addExamplePacks);
    }

    private static void addExamplePacks(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
//            event.addPackFinders(
//                    NovoAtlas.loc("resourcepacks/avila-basic-example"),
//                    PackType.SERVER_DATA,
//                    Component.literal("novoatlas/avila-basic-example"),
//                    PackSource.FEATURE,
//                    false,
//                    Pack.Position.BOTTOM
//            );
            event.addRepositorySource(source -> {
                source.accept(Pack.readMetaAndCreate(
                        NovoAtlas.loc("resourcepacks/avila-basic-example").toString(),
                        Component.literal("novoatlas/avila-basic-example"),
                        false,
                        id -> new PathPackResources(id, ModList.get().getModFileById(NovoAtlas.MOD_ID).getFile().findResource("resourcepacks/avila-basic-example"), false),
                        PackType.SERVER_DATA,
                        Pack.Position.BOTTOM,
                        PackSource.FEATURE
                ));
            });
//
//            event.addPackFinders(
//                    NovoAtlas.loc("resourcepacks/avila-cave-biome-example"),
//                    PackType.SERVER_DATA,
//                    Component.literal("novoatlas/avila-cave-biome-example"),
//                    PackSource.FEATURE,
//                    false,
//                    Pack.Position.BOTTOM
//            );
            event.addRepositorySource(source -> {
                source.accept(Pack.readMetaAndCreate(
                        NovoAtlas.loc("resourcepacks/avila-cave-biome-example").toString(),
                        Component.literal("novoatlas/avila-cave-biome-example"),
                        false,
                        id -> new PathPackResources(id, ModList.get().getModFileById(NovoAtlas.MOD_ID).getFile().findResource("resourcepacks/avila-cave-biome-example"), false),
                        PackType.SERVER_DATA,
                        Pack.Position.BOTTOM,
                        PackSource.FEATURE
                ));
            });
//
//            event.addPackFinders(
//                    NovoAtlas.loc("resourcepacks/avila-no-caves-example"),
//                    PackType.SERVER_DATA,
//                    Component.literal("novoatlas/avila-no-caves-example"),
//                    PackSource.FEATURE,
//                    false,
//                    Pack.Position.BOTTOM
//            );
            event.addRepositorySource(source -> {
                source.accept(Pack.readMetaAndCreate(
                        NovoAtlas.loc("resourcepacks/avila-no-caves-example").toString(),
                        Component.literal("novoatlas/avila-no-caves-example"),
                        false,
                        id -> new PathPackResources(id, ModList.get().getModFileById(NovoAtlas.MOD_ID).getFile().findResource("resourcepacks/avila-no-caves-example"), false),
                        PackType.SERVER_DATA,
                        Pack.Position.BOTTOM,
                        PackSource.FEATURE
                ));
            });
        }
    }

//    private static void register(RegisterEvent event) {
//        event.register(Registries.CHUNK_GENERATOR, NovoAtlas.loc("image_map"), () -> ImageMapChunkGenerator.CODEC);
//        event.register(Registries.BIOME_SOURCE, NovoAtlas.loc("color_map"), () -> ColorMapBiomeSource.CODEC);
//        event.register(Registries.DENSITY_FUNCTION_TYPE, NovoAtlas.loc("heightmap"), () -> HeightmapDensityFunction.DATA_CODEC);
//    }

    private static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(NovoAtlasResourceKeys.MAP_INFO, MapInfo.DIRECT_CODEC);
    }

    private static void registerResourceReloader(AddReloadListenerEvent event) {
        event.addListener(new MapImageLoader());
    }

    static {
        CHUNK_GENERATORS.register("image_map", () -> ImageMapChunkGenerator.CODEC);
        BIOME_SOURCES.register("color_map", () -> ColorMapBiomeSource.CODEC);
        DENSITY_FUNCTIONS.register("heightmap", () -> HeightmapDensityFunction.DATA_CODEC);
    }
}
