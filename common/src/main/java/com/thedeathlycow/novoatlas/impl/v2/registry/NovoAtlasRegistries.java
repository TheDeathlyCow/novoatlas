package com.thedeathlycow.novoatlas.impl.v2.registry;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.HeightMap;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ColorImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.data.GrayscaleImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.Interpolator;
import com.thedeathlycow.novoatlas.impl.v2.image.sampler.ImageSampler;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class NovoAtlasRegistries {
    public static final ResourceKey<Registry<GrayscaleImageData>> GRAYSCALE_IMAGES = ResourceKey.createRegistryKey(NovoAtlas.id("images/grayscale"));

    public static final ResourceKey<Registry<ColorImageData>> COLOR_IMAGES = ResourceKey.createRegistryKey(NovoAtlas.id("images/color"));

    public static final ResourceKey<Registry<HeightMap>> HEIGHT_MAP = ResourceKey.createRegistryKey(NovoAtlas.id("maps/height_map"));

    public static final ResourceKey<Registry<MapCodec<? extends ImageSampler>>> IMAGE_SAMPLER_TYPE = ResourceKey.createRegistryKey(NovoAtlas.id("image_sampler_type"));

    public static final ResourceKey<Registry<MapCodec<? extends Interpolator>>> INTERPOLATOR_TYPE = ResourceKey.createRegistryKey(NovoAtlas.id("interpolator_type"));

    private NovoAtlasRegistries() {
    }
}