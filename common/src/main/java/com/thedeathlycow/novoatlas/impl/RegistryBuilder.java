package com.thedeathlycow.novoatlas.impl;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class RegistryBuilder {
    @ExpectPlatform
    public static <T> Registry<T> createBuiltinRegistry(ResourceKey<Registry<T>> key) {
        throw new AssertionError();
    }
}