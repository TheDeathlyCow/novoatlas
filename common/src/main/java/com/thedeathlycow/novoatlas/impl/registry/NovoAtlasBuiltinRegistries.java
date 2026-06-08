package com.thedeathlycow.novoatlas.impl.registry;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.platform.Services;
import com.thedeathlycow.novoatlas.impl.gen.interpolation.Interpolator;
import net.minecraft.core.Registry;

public final class NovoAtlasBuiltinRegistries {
    public static final Registry<MapCodec<? extends Interpolator>> INTERPOLATOR_TYPE = Services.PLATFORM.createBuiltinRegistry(NovoAtlasRegistries.INTERPOLATOR_TYPE);

    private NovoAtlasBuiltinRegistries() {

    }
}