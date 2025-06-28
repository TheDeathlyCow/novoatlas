package com.thedeathlycow.novoatlas.registry.fabric;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class NovoAtlasRegistriesImpl {
    public static <T> Registry<T> createSimpleRegistry(ResourceKey<Registry<T>> key) {
        return FabricRegistryBuilder.createSimple(key).buildAndRegister();
    }
}