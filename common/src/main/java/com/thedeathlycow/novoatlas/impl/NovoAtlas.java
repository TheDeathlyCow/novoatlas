package com.thedeathlycow.novoatlas.impl;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NovoAtlas {
    public static final String MOD_ID = "novoatlas";
    public static final String MOD_NAME = "NovoAtlas";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final boolean ENABLE_EXAMPLE_PACKS = Boolean.parseBoolean(System.getProperty("novoatlas.enable-example-packs"));;

    public static void init() {
        // Write common init code here.
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static Identifier expId(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID + "-experimental", path);
    }

    public static boolean enableExampleDataPacks() {
        return ENABLE_EXAMPLE_PACKS;
    }

    private NovoAtlas() {

    }
}
