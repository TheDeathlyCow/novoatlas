package com.thedeathlycow.novoatlas.registry;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.platform.Services;
import com.thedeathlycow.novoatlas.world.gen.interpolation.Interpolator;
import net.minecraft.core.Registry;

public final class NovoAtlasBuiltinRegistries {
    public static final Registry<MapCodec<? extends Interpolator>> INTERPOLATOR_TYPE = Services.PLATFORM.createBuiltinRegistry(NovoAtlasRegistries.INTERPOLATOR_TYPE);

    private NovoAtlasBuiltinRegistries() {

    }
}