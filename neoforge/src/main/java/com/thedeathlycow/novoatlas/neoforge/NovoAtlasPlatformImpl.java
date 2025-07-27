package com.thedeathlycow.novoatlas.neoforge;

import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;

import java.nio.file.Path;

public class NovoAtlasPlatformImpl {
    public static boolean isModLoaded(String modid) {
        return LoadingModList.get().getModFileById(modid) != null;
    }

    public static Path getGameDirectory() {
        return FMLPaths.GAMEDIR.get();
    }
}