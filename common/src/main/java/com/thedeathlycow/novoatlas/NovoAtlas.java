package com.thedeathlycow.novoatlas;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public final class NovoAtlas {
    public static final String MOD_ID = "novoatlas";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static Path cacheDirectory;

    public static void init() {
        // Write common init code here.
    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static Path getCacheDirectory() {
        if (cacheDirectory == null) {
            cacheDirectory = NovoAtlasPlatform.getGameDirectory().resolve(MOD_ID);
            try {
                Files.createDirectories(cacheDirectory);
            } catch (Exception e) {
                LOGGER.error("Failed to create cache directory: {}", cacheDirectory, e);
            }
        }
        return cacheDirectory;
    }

    private NovoAtlas() {

    }
}
