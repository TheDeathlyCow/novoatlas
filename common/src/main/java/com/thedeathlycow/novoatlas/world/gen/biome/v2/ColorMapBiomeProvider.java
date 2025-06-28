package com.thedeathlycow.novoatlas.world.gen.biome.v2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.registry.NovoAtlasResourceKeys;
import com.thedeathlycow.novoatlas.world.gen.MapImage;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import com.thedeathlycow.novoatlas.world.gen.biome.BiomeColorEntry;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ColorMapBiomeProvider implements BiomeMapProvider {
    public static final MapCodec<ColorMapBiomeProvider> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    ResourceKey.codec(NovoAtlasResourceKeys.BIOME_MAP)
                            .fieldOf("map")
                            .forGetter(ColorMapBiomeProvider::getMap),
                    BiomeColorEntry.LIST_CODEC
                            .fieldOf("biomes")
                            .forGetter(ColorMapBiomeProvider::getBiomeColors),
                    Codec.BOOL
                            .optionalFieldOf("strict", false)
                            .forGetter(ColorMapBiomeProvider::isStrict)
            ).apply(instance, ColorMapBiomeProvider::new)
    );

    private final ResourceKey<MapImage> map;
    private final List<BiomeColorEntry> biomeColors;
    private final boolean strict;
    private final Int2ObjectMap<Holder<Biome>> biomeToColorCache = new Int2ObjectArrayMap<>();

    public ColorMapBiomeProvider(ResourceKey<MapImage> map, List<BiomeColorEntry> biomeColors, boolean strict) {
        this.map = map;
        this.biomeColors = biomeColors;
        this.strict = strict;

        for (BiomeColorEntry entry : biomeColors) {
            this.biomeToColorCache.put(entry.color(), entry.biome());
        }
    }

    @Override
    @Nullable
    public Holder<Biome> getBiome(int x, int y, int z, MapInfo info) {
        MapImage image = MapInfo.lookupBiomeMap(this.map);
        int color = image.sample(x, z, info, Integer.MIN_VALUE);

        if (color == -1) {
            return null;
        }

        Holder<Biome> mappedBiome = this.biomeToColorCache.get(color);

        if (mappedBiome != null) {
            return mappedBiome;
        } else if (strict) {
            return null;
        } else {
            return this.getClosest(color);
        }
    }

    @Override
    public MapCodec<? extends ColorMapBiomeProvider> getCodec() {
        return CODEC;
    }

    public ResourceKey<MapImage> getMap() {
        return map;
    }

    public List<BiomeColorEntry> getBiomeColors() {
        return biomeColors;
    }

    public boolean isStrict() {
        return strict;
    }

    @Nullable
    private Holder<Biome> getClosest(int color) {
        double closestDistance = Integer.MAX_VALUE;
        int closest = -1;

        int red = red(color);
        int green = green(color);
        int blue = blue(color);

        for (int candidate : this.biomeToColorCache.keySet()) {
            int dRed = red(candidate) - red;
            int dGreen = green(candidate) - green;
            int dBlue = blue(candidate) - blue;

            double candidateDistance = dRed * dRed + dGreen * dGreen + dBlue * dBlue;

            if (candidateDistance < closestDistance) {
                closestDistance = candidateDistance;
                closest = candidate;
            }
        }

        return this.biomeToColorCache.getOrDefault(closest, null);
    }

    private static int red(int color) {
        return color & 0xFF0000 >> 16;
    }

    private static int green(int color) {
        return color & 0xFF00 >> 8;
    }

    private static int blue(int color) {
        return color & 0xFF;
    }
}