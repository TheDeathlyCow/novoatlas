package com.thedeathlycow.novoatlas.world.gen.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class NovoAtlasBiomeSource extends BiomeSource {
    public static final MapCodec<NovoAtlasBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(NovoAtlasBiomeSource::getMapInfo),
                    BiomeColorEntry.CODEC.listOf()
                            .fieldOf("biomes")
                            .forGetter(NovoAtlasBiomeSource::getBiomeColors),
                    Biome.CODEC
                            .fieldOf("default_biome")
                            .forGetter(NovoAtlasBiomeSource::getDefaultBiome),
                    BiomeSource.CODEC
                            .optionalFieldOf("cave_biomes")
                            .forGetter(NovoAtlasBiomeSource::getCaveBiomes),
                    Codec.INT
                            .optionalFieldOf("below_depth", Integer.MAX_VALUE)
                            .forGetter(NovoAtlasBiomeSource::getCaveBiomeDepth)
            ).apply(instance, NovoAtlasBiomeSource::new)
    );

    private final Holder<MapInfo> mapInfo;

    private final List<BiomeColorEntry> biomeColors;

    private final Holder<Biome> defaultBiome;

    private final Optional<BiomeSource> caveBiomes;

    private final int caveBiomeDepth;

    private final Int2ObjectMap<Holder<Biome>> biomeToColorCache = new Int2ObjectArrayMap<>();

    public NovoAtlasBiomeSource(
            Holder<MapInfo> mapInfo,
            List<BiomeColorEntry> biomeColors,
            Holder<Biome> defaultBiome,
            Optional<BiomeSource> caveBiomes,
            int caveBiomeDepth
    ) {
        this.mapInfo = mapInfo;
        this.biomeColors = biomeColors;
        this.defaultBiome = defaultBiome;
        this.caveBiomes = caveBiomes;
        this.caveBiomeDepth = caveBiomeDepth;

        for (BiomeColorEntry entry : biomeColors) {
            this.biomeToColorCache.put(entry.color(), entry.biome());
        }
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return null;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        Stream<Holder<Biome>> stream = Stream.concat(
                Stream.of(defaultBiome),
                biomeColors
                        .stream()
                        .map(BiomeColorEntry::biome)
        );

        if (this.caveBiomes.isPresent()) {
            Stream<Holder<Biome>> caveStream = this.caveBiomes.orElseThrow()
                    .possibleBiomes()
                    .stream();
            return Stream.concat(stream, caveStream);
        }

        return stream;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler sampler) {
        MapInfo info = this.mapInfo.value();
        int x = QuartPos.toBlock(biomeX);
        int y = QuartPos.toBlock(biomeY);
        int z = QuartPos.toBlock(biomeZ);

        int elevation = info.getHeightMapElevation(x, z);
        if (y < elevation - this.caveBiomeDepth && caveBiomes.isPresent()) {
            return caveBiomes.orElseThrow().getNoiseBiome(biomeX, biomeY, biomeZ, sampler);
        }

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

    public Optional<BiomeSource> getCaveBiomes() {
        return caveBiomes;
    }

    public int getCaveBiomeDepth() {
        return caveBiomeDepth;
    }
}