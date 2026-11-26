package com.thedeathlycow.novoatlas.impl.v1.registry;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.platform.Services;
import com.thedeathlycow.novoatlas.impl.v1.image.interpolation.InterpolatorV1;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.Interpolator;
import com.thedeathlycow.novoatlas.impl.v2.image.sampler.ImageSampler;
import com.thedeathlycow.novoatlas.impl.v2.registry.NovoAtlasRegistries;
import net.minecraft.core.Registry;

public final class NovoAtlasBuiltinRegistriesV1 {
    public static final Registry<MapCodec<? extends InterpolatorV1>> INTERPOLATOR_TYPE = Services.PLATFORM.createBuiltinRegistry(NovoAtlasRegistriesV1.INTERPOLATOR_TYPE);

    private NovoAtlasBuiltinRegistriesV1() {

    }
}