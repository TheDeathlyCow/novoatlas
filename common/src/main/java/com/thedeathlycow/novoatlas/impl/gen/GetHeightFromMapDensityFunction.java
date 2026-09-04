package com.thedeathlycow.novoatlas.impl.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DensityFunction;

public record GetHeightFromMapDensityFunction(
        Holder<MapInfo> mapInfo,
        int lowerBound,
        int upperBound
) implements DensityFunction.SimpleFunction {
    public static final MapCodec<GetHeightFromMapDensityFunction> DATA_CODEC = RecordCodecBuilder.<GetHeightFromMapDensityFunction>mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(GetHeightFromMapDensityFunction::mapInfo),
                    Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2)
                            .fieldOf("lower_bound")
                            .forGetter(GetHeightFromMapDensityFunction::lowerBound),
                    Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2)
                            .fieldOf("upper_bound")
                            .forGetter(GetHeightFromMapDensityFunction::upperBound)
            ).apply(instance, GetHeightFromMapDensityFunction::new)
    ).validate(df -> {
        if (df.upperBound < df.lowerBound) {
            return DataResult.error(() -> "Upper bound " + df.upperBound + " is less than lower bound " + df.lowerBound);
        } else {
            return DataResult.success(df);
        }
    });

    public static final KeyDispatchDataCodec<GetHeightFromMapDensityFunction> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        int elevation = mapInfo.value().getHeightMapElevation(context.blockX(), context.blockZ(), Integer.MIN_VALUE);

        return Mth.clamp(elevation, this.lowerBound, this.upperBound);
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
    public KeyDispatchDataCodec<? extends GetHeightFromMapDensityFunction> codec() {
        return CODEC;
    }
}