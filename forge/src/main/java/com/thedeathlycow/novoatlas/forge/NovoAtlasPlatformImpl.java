package com.thedeathlycow.novoatlas.forge;

import net.minecraftforge.fml.ModList;

public class NovoAtlasPlatformImpl {
    public static boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }
}