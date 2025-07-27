package com.thedeathlycow.novoatlas;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

public class NovoAtlasPlatform {
    @ExpectPlatform
    public static boolean isModLoaded(String modid) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Path getGameDirectory() {
        throw new AssertionError();
    }
}