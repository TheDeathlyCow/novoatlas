package com.thedeathlycow.novoatlas.fabric;

import net.fabricmc.api.ModInitializer;

import com.thedeathlycow.novoatlas.NovoAtlas;

public final class NovoAtlasFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NovoAtlas.init();
    }
}
