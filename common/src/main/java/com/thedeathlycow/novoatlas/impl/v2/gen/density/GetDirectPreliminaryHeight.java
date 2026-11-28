package com.thedeathlycow.novoatlas.impl.v2.gen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.HeightMap;
import com.thedeathlycow.novoatlas.impl.v2.image.data.GrayscaleImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.ImageTransform;
import net.minecraft.core.Holder;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.densityfunction.*;
import org.jspecify.annotations.NonNull;

public record GetDirectPreliminaryHeight(
        Holder<HeightMap> heightMap,
        ImageTransform transform,
        int lowerBound,
        int upperBound
) implements DensityFunction {
    public static final MapCodec<GetDirectPreliminaryHeight> CODEC = RecordCodecBuilder.<GetDirectPreliminaryHeight>mapCodec(
            instance -> instance.group(
                    HeightMap.CODEC
                            .fieldOf("height_map")
                            .forGetter(i -> i.heightMap),
                    ImageTransform.CODEC
                            .optionalFieldOf("transform", ImageTransform.DEFAULT)
                            .forGetter(i -> i.transform),
                    Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2)
                            .fieldOf("lower_bound")
                            .forGetter(i -> i.lowerBound),
                    Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2)
                            .fieldOf("upper_bound")
                            .forGetter(i -> i.upperBound)
            ).apply(instance, GetDirectPreliminaryHeight::new)
    ).validate(df -> {
        if (df.upperBound < df.lowerBound) {
            return DataResult.error(() -> "Upper bound " + df.upperBound + " is less than lower bound " + df.lowerBound);
        } else {
            return DataResult.success(df);
        }
    });

    @Override
    @NonNull
    public DensitySampler compileSampler(CompileContext context) {
        return new Sampler(this.heightMap.value(), this.transform, this.lowerBound, this.upperBound);
    }

    @Override
    @NonNull
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return this;
    }

    @Override
    @NonNull
    public Interval range() {
        return Interval.of(this.lowerBound, this.upperBound);
    }

    @Override
    public @Axes int domainAxes() {
        return DensityFunction.AXIS_X | DensityFunction.AXIS_Z;
    }

    @Override
    @NonNull
    public MapCodec<? extends GetDirectPreliminaryHeight> codec() {
        return CODEC;
    }

    private record Sampler(HeightMap heightMap, ImageTransform transform, int minValue,
                           int maxValue) implements DensitySampler {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            int index = 0;
            ImageLoader<GrayscaleImageData> loader = LerpHeightMap.getLoader(context);

            for (int z = 0; z < volume.sizeZ(); z++) {
                for (int x = 0; x < volume.sizeX(); x++) {
                    final int elevation = this.heightMap.getHeightValue(volume.blockX(x), volume.blockZ(z), loader, this.transform) - 8;
                    final float density = Mth.clamp(elevation, this.minValue, this.maxValue);

                    for (int y = 0; y < volume.sizeY(); y++) {
                        outputBuffer.set(index, density);
                        index++;
                    }
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            ImageLoader<GrayscaleImageData> loader = LerpHeightMap.getLoader(context);
            final int elevation = this.heightMap.getHeightValue(blockX, blockZ, loader, this.transform) - 8;
            return Mth.clamp(elevation, this.minValue, this.maxValue);
        }
    }
}