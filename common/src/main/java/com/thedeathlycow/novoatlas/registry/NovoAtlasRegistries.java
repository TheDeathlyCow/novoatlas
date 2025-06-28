package com.thedeathlycow.novoatlas.registry;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.world.gen.biome.v2.BiomeMapProvider;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;


public final class NovoAtlasRegistries {
    public static final Registry<MapCodec<? extends BiomeMapProvider>> BIOME_MAP_PROVIDER = createSimpleRegistry(
            NovoAtlasResourceKeys.BIOME_MAP_PROVIDER
    );

    @ExpectPlatform
    public static <T> Registry<T> createSimpleRegistry(ResourceKey<Registry<T>> key) {
        throw new AssertionError();
    }

    private NovoAtlasRegistries() {

    }
}