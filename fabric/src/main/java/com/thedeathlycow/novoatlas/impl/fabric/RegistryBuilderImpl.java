package com.thedeathlycow.novoatlas.impl.fabric;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class RegistryBuilderImpl {
    public static <T> Registry<T> createBuiltinRegistry(ResourceKey<Registry<T>> key) {
        return FabricRegistryBuilder.createSimple(key).buildAndRegister();
    }
}