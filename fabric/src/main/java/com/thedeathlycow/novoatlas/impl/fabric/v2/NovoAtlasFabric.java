package com.thedeathlycow.novoatlas.impl.fabric.v2;

import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.Bicubic;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.Bilinear;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.Lanczos;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.NearestNeighbour;
import com.thedeathlycow.novoatlas.impl.v2.registry.NovoAtlasBuiltinRegistries;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.HeightMap;
import com.thedeathlycow.novoatlas.impl.v2.image.sampler.SingleImageSampler;
import com.thedeathlycow.novoatlas.impl.v2.registry.NovoAtlasRegistries;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public class NovoAtlasFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NovoAtlas.init();

        DynamicRegistries.register(NovoAtlasRegistries.HEIGHT_MAP, HeightMap.DIRECT_CODEC);

        Registry.register(NovoAtlasBuiltinRegistries.IMAGE_SAMPLER_TYPE, NovoAtlas.id("single_image"), SingleImageSampler.CODEC);

        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("nearest_neighbor"), NearestNeighbour.CODEC);
        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bilinear"), Bilinear.CODEC);
        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("bicubic"), Bicubic.CODEC);
        Registry.register(NovoAtlasBuiltinRegistries.INTERPOLATOR_TYPE, NovoAtlas.id("lanczos"), Lanczos.CODEC);

        ModContainer mod = FabricLoader.getInstance().getModContainer(NovoAtlas.MOD_ID).orElseThrow();

        PackActivationType activation = NovoAtlas.enableExampleDataPacks() ? PackActivationType.DEFAULT_ENABLED : PackActivationType.NORMAL;

        ResourceLoader.registerBuiltinPack(NovoAtlas.id("avila-basic-example"), mod, activation);
        ResourceLoader.registerBuiltinPack(NovoAtlas.id("avila-blend-to-random-example"), mod, activation);
        ResourceLoader.registerBuiltinPack(NovoAtlas.id("avila-cave-biome-example"), mod, activation);
        ResourceLoader.registerBuiltinPack(NovoAtlas.id("avila-no-caves-example"), mod, activation);
    }

    private static void addDefaultAlias(Registry<?> registry, Identifier id) {
        registry.addAlias(Identifier.withDefaultNamespace(id.getPath()), id);
    }
}