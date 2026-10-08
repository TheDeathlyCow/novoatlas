package com.thedeathlycow.novoatlas.impl.gen.biome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.gen.density.BlendedSampler;
import com.thedeathlycow.novoatlas.impl.image.BiomeMapImage;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import com.thedeathlycow.novoatlas.impl.image.biome.provider.LayeredMapBiomeProvider;
import com.thedeathlycow.novoatlas.mixin.accessor.BiomeSourceAccessor;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
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
                            .forGetter(BoundedMapBiomeSource::getBlendDistance),
                    NormalNoise.CODEC
                            .optionalFieldOf("normal_noise", Holder.direct(NoiseData.DEFAULT_SHIFT))
                            .forGetter(BoundedMapBiomeSource::getBlendNoise)
            ).apply(instance, BoundedMapBiomeSource::new)
    );

    private final Holder<MapInfo> mapInfo;
    private final Holder<Biome> defaultBiome;
    private final BiomeSource outsideMap;
    private final float blendDistance;
    private final Holder<NormalNoise> blendNoise;

    public BoundedMapBiomeSource(
            Holder<MapInfo> mapInfo,
            Holder<Biome> defaultBiome,
            BiomeSource outsideMap,
            float blendDistance,
            Holder<NormalNoise> blendNoise
    ) {
        this.mapInfo = mapInfo;
        this.defaultBiome = defaultBiome;
        this.outsideMap = outsideMap;
        this.blendDistance = blendDistance;
        this.blendNoise = blendNoise;
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
        BiomeResolver insideMapResolver = this.mapInfo.value().createBiomeResolver(this.defaultBiome);
        BiomeResolver outsideMapResolver = this.outsideMap.createResolver(sampler);
        return new BlendedBiomeResolver(
                this.mapInfo.value(),
                this.defaultBiome,
                insideMapResolver,
                outsideMapResolver,
                this.blendDistance,
                this.blendNoise.value().create(new XoroshiroRandomSource(67L))
        );
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

    public Holder<NormalNoise> getBlendNoise() {
        return blendNoise;
    }

    private record BlendedBiomeResolver(
            MapInfo mapInfo,
            Holder<Biome> defaultBiome,
            BiomeResolver insideMapResolver,
            BiomeResolver outsideMapResolver,
            float blendDistance,
            Noise blendNoise
    ) implements BiomeResolver {
        @Override
        public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ) {
            BiomeMapImage surfaceBiomes = this.mapInfo.getSurfaceBiomeMap();

            if (surfaceBiomes.isBlockInsideImage(quartX, quartZ, this.mapInfo)) {
                return this.insideMapResolver.getNoiseBiome(quartX, quartY, quartZ);
            }

            float distanceToEdge = (float) surfaceBiomes.getDistanceToEdge(quartX, quartZ, this.mapInfo);

            float alpha = BlendedSampler.smoothstep(0, this.blendDistance, distanceToEdge);

            if (alpha >= 1.0f) {
                return this.outsideMapResolver.getNoiseBiome(quartX, quartY, quartZ);
            }

            alpha = BlendedSampler.smoothstep(0f, 1f, alpha);

            float noise = Mth.clamp(this.generateNoise(quartX, quartZ, 0.25f, 0.15f), 0f, 1f);

            if (noise < alpha) {
                return this.outsideMapResolver.getNoiseBiome(quartX, quartY, quartZ);
            }

            return this.insideMapResolver.getNoiseBiome(
                    Mth.floor(this.generateNoise(quartX, quartZ, quartX, 4f)),
                    quartY,
                    Mth.floor(this.generateNoise(quartX, quartZ, quartZ, 4f))
            );
        }

        private float generateNoise(int x, int z, float offset, float blurriness) {
            return offset + this.blendNoise.get(x, 0, z) * blurriness;
        }
    }
}