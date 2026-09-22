package com.thedeathlycow.novoatlas.impl.fabric;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class NovoAtlasPlatformImpl {
    public static boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    public static <T> Registry<T> createBuiltinRegistry(ResourceKey<Registry<T>> key) {
        return FabricRegistryBuilder.createSimple(key).buildAndRegister();
    }
}