package com.thedeathlycow.novoatlas.impl.v2.gen.density;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.HeightMap;
import com.thedeathlycow.novoatlas.impl.v2.image.data.GrayscaleImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.ImageTransform;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.densityfunction.*;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public record LerpHeightMap(
        Holder<HeightMap> heightMap,
        ImageTransform transform,
        int blockRange
) implements DensityFunction {
    public static final MapCodec<LerpHeightMap> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    HeightMap.CODEC
                            .fieldOf("map_info")
                            .forGetter(i -> i.heightMap),
                    ImageTransform.CODEC
                            .optionalFieldOf("transform", ImageTransform.DEFAULT)
                            .forGetter(i -> i.transform),
                    ExtraCodecs.POSITIVE_INT
                            .optionalFieldOf("range", 10)
                            .forGetter(i -> i.blockRange)
            ).apply(instance, LerpHeightMap::new)
    );

    @Override
    @NonNull
    public DensitySampler compileSampler(CompileContext context) {
        return new LerpHeightMap.Sampler(this.heightMap.value(), this.transform, this.blockRange);
    }

    @Override
    @NonNull
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return this;
    }

    @Override
    @NonNull
    public Interval range() {
        return Interval.of(-1, 1);
    }

    @Override
    @Axes
    public int domainAxes() {
        return DensityFunction.ALL_AXES;
    }

    @Override
    @NonNull
    public MapCodec<? extends LerpHeightMap> codec() {
        return CODEC;
    }

    public static ImageLoader<GrayscaleImageData> getLoader(SamplerContext context) {
        return Objects.requireNonNull(context.getField(ImageLoader.GRAYSCALE_CONTEXT), "Grayscale context not defined!");
    }

    private record Sampler(HeightMap heightMap, ImageTransform transform, int blockRange) implements DensitySampler {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            int index = 0;
            ImageLoader<GrayscaleImageData> loader = getLoader(context);

            for (int z = 0; z < volume.sizeZ(); z++) {
                for (int x = 0; x < volume.sizeX(); x++) {
                    final int elevation = this.heightMap.getHeightValue(volume.blockX(x), volume.blockZ(z), loader, this.transform);

                    for (int y = 0; y < volume.sizeY(); y++) {
                        float yOffset = Mth.clamp(elevation - volume.blockY(y), -blockRange, blockRange);
                        float density = yOffset / blockRange;
                        outputBuffer.set(index, density);

                        index++;
                    }
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            ImageLoader<GrayscaleImageData> loader = getLoader(context);
            int elevation = this.heightMap.getHeightValue(blockX, blockZ, loader, this.transform);
            float yOffset = Mth.clamp(elevation - blockY, -blockRange, blockRange);
            return yOffset / blockRange;
        }
    }
}