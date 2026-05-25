package com.thedeathlycow.novoatlas.fabric;

import com.thedeathlycow.novoatlas.platform.NovoAtlasPlatform;
import net.fabricmc.loader.api.FabricLoader;

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
}