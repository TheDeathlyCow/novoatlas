package com.thedeathlycow.novoatlas.impl.neoforge;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class RegistryBuilderImpl {
    public static <T> Registry<T> createBuiltinRegistry(ResourceKey<Registry<T>> key) {
        return new RegistryBuilder<>(key).create();
    }
}