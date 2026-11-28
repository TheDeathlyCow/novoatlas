package com.thedeathlycow.novoatlas.impl.v1.registry;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.platform.Services;
import com.thedeathlycow.novoatlas.impl.v1.image.interpolation.Interpolator;
import net.minecraft.core.Registry;

public final class NovoAtlasBuiltinRegistries {
    public static final Registry<MapCodec<? extends Interpolator>> INTERPOLATOR_TYPE = Services.PLATFORM.createBuiltinRegistry(NovoAtlasRegistries.LEGACY_INTERPOLATOR_TYPE);

    private NovoAtlasBuiltinRegistries() {

    }
}