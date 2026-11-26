package com.thedeathlycow.novoatlas.impl.v2.registry;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.platform.Services;
import com.thedeathlycow.novoatlas.impl.v2.image.sampler.ImageSampler;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.Interpolator;
import net.minecraft.core.Registry;

public final class NovoAtlasBuiltinRegistries {
    public static final Registry<MapCodec<? extends ImageSampler>> IMAGE_SAMPLER_TYPE = Services.PLATFORM.createBuiltinRegistry(NovoAtlasRegistries.IMAGE_SAMPLER_TYPE);

    public static final Registry<MapCodec<? extends Interpolator>> INTERPOLATOR_TYPE = Services.PLATFORM.createBuiltinRegistry(NovoAtlasRegistries.INTERPOLATOR_TYPE);

    private NovoAtlasBuiltinRegistries() {

    }
}