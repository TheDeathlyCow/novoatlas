package com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.v2.math.ColorHelper;
import com.thedeathlycow.novoatlas.impl.v2.registry.NovoAtlasRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;

import java.util.Map;
import java.util.Optional;

public final class ColorMappingData {
    private static final Codec<Map<String, Optional<Holder<Biome>>>> MAP_CODEC = Codec.unboundedMap(
            ColorHelper.STRING_CODEC,
            ExtraCodecs.optionalEmptyMap(Biome.CODEC)
    ).validate(map -> map.isEmpty()
            ? DataResult.error(() -> "biomes cannot be empty!")
            : DataResult.success(map)
    );
    public static final Codec<ColorMappingData> DIRECT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    MAP_CODEC
                            .fieldOf("biomes")
                            .forGetter(i -> i.biomes)
            ).apply(instance, ColorMappingData::new)
    );
    public static final Codec<Holder<ColorMappingData>> CODEC = RegistryCodecs.holder(NovoAtlasRegistries.COLOR_MAPPING, DIRECT_CODEC, true);
    public static final Codec<HolderSet<ColorMappingData>> LIST_CODEC = RegistryCodecs.holderSet(NovoAtlasRegistries.COLOR_MAPPING, ColorMappingData.DIRECT_CODEC);

    private final Map<String, Optional<Holder<Biome>>> biomes;

    public ColorMappingData(Map<String, Optional<Holder<Biome>>> biomes) {
        this.biomes = biomes;
    }

    public Map<String, Optional<Holder<Biome>>> biomes() {
        return biomes;
    }
}