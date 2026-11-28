package com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.biome;

import com.thedeathlycow.novoatlas.impl.v2.math.ColorHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public final class BoundColorMapping {
    private final Int2ObjectMap<Optional<Holder<Biome>>> cache;

    private BoundColorMapping(Int2ObjectMap<Optional<Holder<Biome>>> cache) {
        this.cache = cache.size() > 8
                ? new Int2ObjectArrayMap<>(cache)
                : new Int2ObjectOpenHashMap<>(cache);
    }

    public static BoundColorMapping fromData(HolderSet<ColorMappingData> data) {
        var cache = new Int2ObjectOpenHashMap<Optional<Holder<Biome>>>();

        for (Holder<ColorMappingData> datum : data) {
            updateCache(cache, datum.value());
        }

        return new BoundColorMapping(cache);
    }

    private static void updateCache(Int2ObjectMap<Optional<Holder<Biome>>> cache, ColorMappingData data) {
        for (Map.Entry<String, Optional<Holder<Biome>>> entry : data.biomes().entrySet()) {
            int color = ColorHelper.toRGB(entry.getKey());
            cache.put(color, entry.getValue());
        }
    }

    @NonNull
    public Holder<Biome> getBiome(int color, boolean strict, Holder<Biome> defaultBiome) {
        if (!this.cache.containsKey(color)) {
            return strict ? defaultBiome : this.getClosest(color).orElse(defaultBiome);
        } else {
            return this.cache.get(color).orElse(defaultBiome);
        }
    }

    private Optional<Holder<Biome>> getClosest(int color) {
        double closestDistance = Integer.MAX_VALUE;
        int closest = -1;

        int red = ARGB.red(color);
        int green = ARGB.green(color);
        int blue = ARGB.blue(color);

        for (int candidate : this.cache.keySet()) {
            int dRed = ARGB.red(candidate) - red;
            int dGreen = ARGB.green(candidate) - green;
            int dBlue = ARGB.blue(candidate) - blue;

            int candidateDistance = dRed * dRed + dGreen * dGreen + dBlue * dBlue;

            if (candidateDistance < closestDistance) {
                closestDistance = candidateDistance;
                closest = candidate;
            }
        }

        return this.cache.get(closest);
    }
}