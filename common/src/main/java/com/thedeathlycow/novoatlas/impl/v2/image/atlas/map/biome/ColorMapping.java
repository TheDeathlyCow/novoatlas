package com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.v2.math.ColorHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public final class ColorMapping {
    private static final Codec<Map<String, Optional<Holder<Biome>>>> MAP_CODEC = Codec.unboundedMap(
            ColorHelper.STRING_CODEC,
            ExtraCodecs.optionalEmptyMap(Biome.CODEC)
    ).validate(map -> map.isEmpty()
            ? DataResult.error(() -> "biomes cannot be empty!")
            : DataResult.success(map)
    );

    public static final Codec<ColorMapping> DIRECT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    MAP_CODEC
                            .fieldOf("biomes")
                            .forGetter(i -> i.biomes),
                    Codec.BOOL
                            .optionalFieldOf("strict", false)
                            .forGetter(i -> i.strict)
            ).apply(instance, ColorMapping::new)
    );

    private final Map<String, Optional<Holder<Biome>>> biomes;
    private final Int2ObjectMap<Optional<Holder<Biome>>> cache;
    private final boolean strict;

    public ColorMapping(Map<String, Optional<Holder<Biome>>> biomes, boolean strict) {
        this.biomes = biomes;
        this.strict = strict;
        this.cache = biomes.size() > 8
                ? new Int2ObjectArrayMap<>()
                : new Int2ObjectOpenHashMap<>();
        this.populateCache();
    }

    @Nullable
    public Holder<Biome> getBiome(int color, @Nullable Holder<Biome> surfaceBiome) {
        if (!this.cache.containsKey(color)) {
            return strict ? null : this.getClosest(color).orElse(surfaceBiome);
        } else {
            return this.cache.get(color).orElse(surfaceBiome);
        }
    }

    private void populateCache() {
        for (Map.Entry<String, Optional<Holder<Biome>>> entry : this.biomes.entrySet()) {
            int color = ColorHelper.toRGB(entry.getKey());
            this.cache.put(color, entry.getValue());
        }
    }

    private Optional<Holder<Biome>> getClosest(int color) {
        double closestDistance = Integer.MAX_VALUE;
        int closest = -1;

        int red = red(color);
        int green = green(color);
        int blue = blue(color);

        for (int candidate : this.cache.keySet()) {
            int dRed = red(candidate) - red;
            int dGreen = green(candidate) - green;
            int dBlue = blue(candidate) - blue;

            double candidateDistance = dRed * dRed + dGreen * dGreen + dBlue * dBlue;

            if (candidateDistance < closestDistance) {
                closestDistance = candidateDistance;
                closest = candidate;
            }
        }

        Optional<Holder<Biome>> biome = this.cache.get(closest);
        this.cache.put(color, biome);
        return biome;
    }


    private static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    private static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    private static int blue(int color) {
        return color & 0xFF;
    }
}