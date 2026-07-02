package com.thedeathlycow.novoatlas.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * Identical to [GetHeightFromMapDensityFunction] but subtracts elevation by 8 to account for the expected preliminary
 * surface value.
 *
 * @param mapInfo
 * @param lowerBound
 * @param upperBound
 */
public record GetPreliminaryHeightFromMapDensityFunction(
        Holder<MapInfo> mapInfo,
        int lowerBound,
        int upperBound
) implements DensityFunction.SimpleFunction {
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
        int elevation = mapInfo.value().getHeightMapElevation(context.blockX(), context.blockZ(), Integer.MIN_VALUE);

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
    public KeyDispatchDataCodec<? extends GetPreliminaryHeightFromMapDensityFunction> codec() {
        return CODEC;
    }
}