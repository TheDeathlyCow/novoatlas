package com.thedeathlycow.novoatlas.impl.v2.image.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.BiomeMap;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.HeightMap;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.ImageTransform;
import net.minecraft.core.Holder;

public final class Atlas {
    public static final Codec<Atlas> DIRECT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ImageTransform.CODEC
                            .optionalFieldOf("transform", ImageTransform.DEFAULT)
                            .forGetter(i -> i.transform),
                    HeightMap.CODEC
                            .fieldOf("height_map")
                            .forGetter(i -> i.heightMap),
                    BiomeMap.CODEC
                            .fieldOf("surface_biome_map")
                            .forGetter(i -> i.surfaceBiomeMap)
            ).apply(instance, Atlas::new)
    );

    private final ImageTransform transform;
    private final Holder<HeightMap> heightMap;
    private final Holder<BiomeMap> surfaceBiomeMap;

    public Atlas(ImageTransform transform, Holder<HeightMap> heightMap, Holder<BiomeMap> surfaceBiomeMap) {
        this.transform = transform;
        this.heightMap = heightMap;
        this.surfaceBiomeMap = surfaceBiomeMap;
    }
}