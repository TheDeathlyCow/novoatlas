package com.thedeathlycow.novoatlas.impl.v2.image.atlas.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.biome.BoundColorMapping;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.biome.ColorMappingData;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ColorImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import com.thedeathlycow.novoatlas.impl.v2.image.sampler.ImageSampler;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.ImageTransform;
import com.thedeathlycow.novoatlas.impl.v2.math.CoordinateSpace;
import com.thedeathlycow.novoatlas.impl.v2.registry.NovoAtlasRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.codec.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

public final class BiomeMap {
    public static final Codec<BiomeMap> DIRECT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ImageTransform.CODEC
                            .optionalFieldOf("transform", ImageTransform.DEFAULT)
                            .forGetter(i -> i.transform),
                    ImageSampler.BASE_CODEC
                            .fieldOf("data")
                            .forGetter(i -> i.data),
                    ColorMappingData.LIST_CODEC
                            .fieldOf("color_mapping")
                            .forGetter(i -> i.mappingData),
                    Codec.BOOL
                            .optionalFieldOf("strict", false)
                            .forGetter(i -> i.strict)
            ).apply(instance, BiomeMap::new)
    );
    public static final Codec<Holder<BiomeMap>> CODEC = RegistryFileCodec.create(NovoAtlasRegistries.BIOME_MAP, DIRECT_CODEC, false);

    private final ImageTransform transform;
    private final ImageSampler data;
    private final HolderSet<ColorMappingData> mappingData;
    private final BoundColorMapping mapping;
    private final boolean strict;

    public BiomeMap(ImageTransform transform, ImageSampler data, HolderSet<ColorMappingData> mappingData, boolean strict) {
        this.transform = transform;
        this.data = data;
        this.mappingData = mappingData;
        this.mapping = BoundColorMapping.fromData(mappingData);
        this.strict = strict;
    }

    @Nullable
    public Holder<Biome> getBiome(
            int quartX,
            int quartY,
            int quartZ,
            ImageLoader<ColorImageData> loader,
            @Nullable Holder<Biome> surfaceBiome,
            ImageTransform baseTransform
    ) {
        double xR = CoordinateSpace.coordToIndexX(quartX, this.transform, this.data.width(loader));
        double zR = CoordinateSpace.coordToIndexX(quartZ, this.transform, this.data.height(loader));

        int color = this.data.getTruncated(xR, zR, this.transform, loader);

        return this.mapping.getBiome(color, strict, surfaceBiome);
    }
}