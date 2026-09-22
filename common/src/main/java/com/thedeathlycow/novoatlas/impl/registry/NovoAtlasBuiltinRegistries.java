package com.thedeathlycow.novoatlas.impl.registry;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.NovoAtlasPlatform;
import com.thedeathlycow.novoatlas.impl.image.interpolation.Interpolator;
import net.minecraft.core.Registry;

public final class NovoAtlasBuiltinRegistries {
    public static final Registry<MapCodec<? extends Interpolator>> INTERPOLATOR_TYPE = NovoAtlasPlatform.createBuiltinRegistry(NovoAtlasRegistries.INTERPOLATOR_TYPE);

    private NovoAtlasBuiltinRegistries() {

    }
}