package com.thedeathlycow.novoatlas.world.gen.biome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public class ColorMapBiomeSource extends BiomeSource {
    public static final MapCodec<ColorMapBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(ColorMapBiomeSource::getMapInfo),
                    BiomeColorEntry.LIST_CODEC
                            .fieldOf("biomes")
                            .forGetter(ColorMapBiomeSource::getBiomeColors),
                    Biome.CODEC
                            .fieldOf("default_biome")
                            .forGetter(ColorMapBiomeSource::getDefaultBiome)
            ).apply(instance, ColorMapBiomeSource::new)
    );

    private final Holder<MapInfo> mapInfo;

    private final List<BiomeColorEntry> biomeColors;

    private final Holder<Biome> defaultBiome;

    private final Int2ObjectMap<Holder<Biome>> biomeToColorCache = new Int2ObjectArrayMap<>();

    public ColorMapBiomeSource(
            Holder<MapInfo> mapInfo,
            List<BiomeColorEntry> biomeColors,
            Holder<Biome> defaultBiome
    ) {
        this.mapInfo = mapInfo;
        this.biomeColors = biomeColors;
        this.defaultBiome = defaultBiome;

        for (BiomeColorEntry entry : biomeColors) {
            this.biomeToColorCache.put(entry.color(), entry.biome());
        }
    }

    @Override
    protected MapCodec<? extends ColorMapBiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(
                Stream.of(defaultBiome),
                biomeColors
                        .stream()
                        .map(BiomeColorEntry::biome)
        );
    }

    @Override
    public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler sampler) {
        MapInfo info = this.mapInfo.value();
        int x = QuartPos.toBlock(biomeX);
        int z = QuartPos.toBlock(biomeZ);

        int color = info.getBiomeColor(x, z, -1);

        if (color == -1) {
            return this.defaultBiome;
        }

        Holder<Biome> mappedBiome = this.biomeToColorCache.get(color);
        return mappedBiome != null ? mappedBiome : this.getClosest(color);
    }

    @NotNull
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

        return this.biomeToColorCache.getOrDefault(closest, this.defaultBiome);
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

    public Holder<MapInfo> getMapInfo() {
        return mapInfo;
    }

    public List<BiomeColorEntry> getBiomeColors() {
        return biomeColors;
    }

    public Holder<Biome> getDefaultBiome() {
        return defaultBiome;
    }
}