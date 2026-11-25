package com.thedeathlycow.novoatlas.impl.v1.gen;

import com.thedeathlycow.novoatlas.impl.v1.NovoAtlas;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;

public final class NovoAtlasDensityFunctions {
    public static final ResourceKey<DensityFunction> CAVES = key("caves");
    public static final ResourceKey<DensityFunction> NO_CAVE_ENTRANCES = key("no_cave_entrances");
    public static final ResourceKey<DensityFunction> NO_CAVES = key("no_caves");

    private static ResourceKey<DensityFunction> key(String path) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, NovoAtlas.id(path));
    }

    private NovoAtlasDensityFunctions() {

    }
}