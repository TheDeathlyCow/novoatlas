package com.thedeathlycow.novoatlas.impl.gen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;

/// Identical to [GetHeightFromMapDensityFunction] but subtracts elevation by 8 to account for the expected preliminary
/// surface value.
public record GetPreliminaryHeightFromMapDensityFunction(
        Holder<MapInfo> mapInfo,
        int lowerBound,
        int upperBound
) implements DensityFunction {
    public static final MapCodec<GetPreliminaryHeightFromMapDensityFunction> DATA_CODEC = RecordCodecBuilder.<GetPreliminaryHeightFromMapDensityFunction>mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(GetPreliminaryHeightFromMapDensityFunction::mapInfo),
                    Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2)
                            .fieldOf("lower_bound")
                            .forGetter(GetPreliminaryHeightFromMapDensityFunction::lowerBound),
                    Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2)
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

    public static final KeyDispatchDataCodec<GetPreliminaryHeightFromMapDensityFunction> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        int elevation = mapInfo.value().getHeightMapElevation(context.blockX(), context.blockZ());
        return Mth.clamp(elevation - 8, this.lowerBound, this.upperBound);
    }

    @Override
    public double minValue() {
        return this.lowerBound;
    }

    @Override
    public double maxValue() {
        return this.upperBound;
    }

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        return null;
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return null;
    }

    @Override
    public Interval range() {
        return null;
    }

    @Override
    public @Axes int domainAxes() {
        return 0;
    }

    @Override
    public KeyDispatchDataCodec<? extends GetPreliminaryHeightFromMapDensityFunction> codec() {
        return CODEC;
    }
}