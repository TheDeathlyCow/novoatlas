package com.thedeathlycow.novoatlas.impl.gen.biome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.gen.density.BlendedSampler;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import com.thedeathlycow.novoatlas.impl.image.biome.provider.LayeredMapBiomeProvider;
import com.thedeathlycow.novoatlas.mixin.accessor.BiomeSourceAccessor;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.Noise;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.stream.Stream;

@ApiStatus.Experimental
public class BoundedMapBiomeSource extends BiomeSource {
    public static final MapCodec<BoundedMapBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(BoundedMapBiomeSource::getMapInfo),
                    Biome.CODEC
                            .fieldOf("default_biome")
                            .forGetter(BoundedMapBiomeSource::getDefaultBiome),
                    BiomeSource.CODEC
                            .fieldOf("outside_map")
                            .forGetter(BoundedMapBiomeSource::getOutsideMap),
                    ExtraCodecs.POSITIVE_FLOAT
                            .fieldOf("blend_distance")
                            .forGetter(BoundedMapBiomeSource::getBlendDistance)
            ).apply(instance, BoundedMapBiomeSource::new)
    );

    private final Holder<MapInfo> mapInfo;
    private final Holder<Biome> defaultBiome;
    private final BiomeSource outsideMap;
    private final float blendDistance;
    private static final Noise SHIFT_NOISE = NoiseData.DEFAULT_SHIFT.create(new XoroshiroRandomSource(42L));

    public BoundedMapBiomeSource(Holder<MapInfo> mapInfo, Holder<Biome> defaultBiome, BiomeSource outsideMap, float blendDistance) {
        this.mapInfo = mapInfo;
        this.defaultBiome = defaultBiome;
        this.outsideMap = outsideMap;
        this.blendDistance = blendDistance;
    }

    @Override
    protected MapCodec<BoundedMapBiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        MapInfo mapInfoValue = this.mapInfo.value();

        Stream<Holder<Biome>> baseBiomes = Stream.concat(
                // this is necessary because java is dumb
                ((BiomeSourceAccessor) outsideMap).invokeCollectPossibleBiomes(),
                mapInfoValue
                        .surfaceBiomes()
                        .collectPossibleBiomes()
        );

        Optional<LayeredMapBiomeProvider> caveBiomes = mapInfoValue.caveBiomes();

        if (caveBiomes.isPresent()) {
            baseBiomes = Stream.concat(
                    baseBiomes,
                    mapInfoValue.caveBiomes().orElseThrow().collectPossibleBiomes()
            );
        }

        return Stream.concat(Stream.of(this.defaultBiome), baseBiomes);
    }

    @Override
    public BiomeResolver createResolver(Climate.Sampler sampler) {
        BiomeResolver outsideMapResolver = this.outsideMap.createResolver(sampler);
        return (quartX, quartY, quartZ) -> this.getNoiseBiome(quartX, quartY, quartZ, outsideMapResolver);
    }

    private Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, BiomeResolver outsideMapResolver) {
        MapInfo info = this.mapInfo.value();

        if (info.isPointInsideBiomeMap(quartX, quartZ)) {
            return info.getBiome(quartX, quartY, quartZ, this.defaultBiome);
        }

        float distanceToEdge = (float) this.mapInfo.value().getBiomeMapDistanceToEdge(quartX, quartZ);

        float alpha = BlendedSampler.smoothstep(0, this.blendDistance, distanceToEdge);

        if (alpha >= 1.0f) {
            return outsideMapResolver.getNoiseBiome(quartX, quartY, quartZ);
        }

        alpha = BlendedSampler.smoothstep(0f, 1f, alpha);
        float noise = Mth.clamp(0.25f + SHIFT_NOISE.get(quartX, 0, quartZ) * 0.5f, 0.0f, 1.0f);

        return noise < alpha
                ? outsideMapResolver.getNoiseBiome(quartX, quartY, quartZ)
                : info.getBiome(quartX, quartY, quartZ, this.defaultBiome);
    }

    public Holder<MapInfo> getMapInfo() {
        return mapInfo;
    }

    public Holder<Biome> getDefaultBiome() {
        return defaultBiome;
    }

    public BiomeSource getOutsideMap() {
        return outsideMap;
    }

    public float getBlendDistance() {
        return blendDistance;
    }
}