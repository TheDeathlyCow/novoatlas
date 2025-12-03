package com.thedeathlycow.novoatlas.forge;

import net.minecraftforge.fml.loading.LoadingModList;

public class NovoAtlasPlatformImpl {
    public static boolean isModLoaded(String modid) {
        return LoadingModList.get().getModFileById(modid) != null;
    }
}