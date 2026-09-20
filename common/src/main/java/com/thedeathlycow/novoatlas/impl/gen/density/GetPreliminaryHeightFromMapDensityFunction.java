package com.thedeathlycow.novoatlas.impl.gen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.image.HeightMapImage;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.densityfunction.*;

/// Identical to [GetHeightFromMapDensityFunction] but subtracts elevation by 8 to account for the expected preliminary
/// surface value.
public record GetPreliminaryHeightFromMapDensityFunction(
        Holder<MapInfo> mapInfo,
        float lowerBound,
        float upperBound
) implements DensityFunction {
    public static final MapCodec<GetPreliminaryHeightFromMapDensityFunction> CODEC = RecordCodecBuilder.<GetPreliminaryHeightFromMapDensityFunction>mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(GetPreliminaryHeightFromMapDensityFunction::mapInfo),
                    Codec.floatRange(DimensionType.MIN_Y * 2f, DimensionType.MAX_Y * 2f)
                            .fieldOf("lower_bound")
                            .forGetter(GetPreliminaryHeightFromMapDensityFunction::lowerBound),
                    Codec.floatRange(DimensionType.MIN_Y * 2f, DimensionType.MAX_Y * 2f)
                            .fieldOf("upper_bound")
                            .forGetter(GetPreliminaryHeightFromMapDensityFunction::upperBound)
            ).apply(instance, GetPreliminaryHeightFromMapDensityFunction::new)
    ).validate(df -> {
        if (df.upperBound < df.lowerBound) {
            return DataResult.error(() -> "Upper bound " + df.upperBound + " is less than lower bound " + df.lowerBound);
        } else {
            return DataResult.success(df);
        }
    });

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        return new Sampler(this.mapInfo.value(), this.lowerBound, this.upperBound);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return this;
    }

    @Override
    public Interval range() {
        return Interval.of(this.lowerBound, this.upperBound);
    }

    @Override
    public @Axes int domainAxes() {
        return DensityFunction.AXIS_X | DensityFunction.AXIS_Z;
    }

    @Override
    public MapCodec<? extends GetPreliminaryHeightFromMapDensityFunction> codec() {
        return CODEC;
    }

    private record Sampler(MapInfo mapInfo, float minValue, float maxValue) implements DensitySampler {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            int index = 0;
            HeightMapImage heightmap = mapInfo.getHeightMap();

            for (int x = 0; x < volume.sizeX(); x++) {
                for (int z = 0; z < volume.sizeZ(); z++) {
                    int elevation = heightmap.sample(volume.blockX(x), volume.blockZ(z), mapInfo) - 8;
                    float density = Mth.clamp(elevation, this.minValue, this.maxValue);
                    outputBuffer.set(index, density);
                    index++;
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            int elevation = mapInfo.getHeightMapElevation(blockX, blockZ) - 8;
            return Mth.clamp(elevation, this.minValue, this.maxValue);
        }
    }
}