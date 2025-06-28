package com.thedeathlycow.novoatlas.registry.neoforge;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class NovoAtlasRegistriesImpl {
    public static <T> Registry<T> createSimpleRegistry(ResourceKey<Registry<T>> key) {
        return new RegistryBuilder<>(key).create();
    }
}