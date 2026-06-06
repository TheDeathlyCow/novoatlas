package com.thedeathlycow.novoatlas.neoforge;

import com.thedeathlycow.novoatlas.platform.NovoAtlasPlatform;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class NovoAtlasNeoForgePlatform implements NovoAtlasPlatform {
    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }

    @Override
    public boolean isEarlyModLoaded(String modid) {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(modid) != null;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public <T> Registry<T> createBuiltinRegistry(ResourceKey<Registry<T>> key) {
        return new RegistryBuilder<>(key).create();
    }
}