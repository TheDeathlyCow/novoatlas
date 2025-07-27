package com.thedeathlycow.novoatlas.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class NovoAtlasPlatformImpl {
    public static boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    public static Path getGameDirectory() {
        return FabricLoader.getInstance().getGameDir();
    }
}