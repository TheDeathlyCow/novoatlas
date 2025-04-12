package com.thedeathlycow.novoatlas.world.gen.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public record BiomeColorEntry(
        Holder<Biome> biome,
        int color
) {
    public static final Codec<BiomeColorEntry> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Biome.CODEC
                            .fieldOf("biome")
                            .forGetter(BiomeColorEntry::biome),
                    ColorHelper.CODEC
                            .fieldOf("color")
                            .forGetter(BiomeColorEntry::color)
            ).apply(instance, BiomeColorEntry::new)
    );
}
