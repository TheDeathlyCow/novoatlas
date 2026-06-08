package com.thedeathlycow.novoatlas.fabric;

import com.thedeathlycow.novoatlas.impl.platform.NovoAtlasPlatform;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class NovoAtlasFabricPlatform implements NovoAtlasPlatform {
    @Override
    public String getPlatformName() {
        return "fabric";
    }

    @Override
    public boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public <T> Registry<T> createBuiltinRegistry(ResourceKey<Registry<T>> key) {
        return FabricRegistryBuilder.create(key).attribute(RegistryAttribute.OPTIONAL).buildAndRegister();
    }
}