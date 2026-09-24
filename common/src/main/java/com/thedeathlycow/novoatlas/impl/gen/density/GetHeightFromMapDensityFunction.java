package com.thedeathlycow.novoatlas.impl.gen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DensityFunction;

public record GetHeightFromMapDensityFunction(
        Holder<MapInfo> mapInfo,
        float lowerBound,
        float upperBound
) implements DensityFunction.SimpleFunction {
    public static final MapCodec<GetHeightFromMapDensityFunction> DATA_CODEC = RecordCodecBuilder.<GetHeightFromMapDensityFunction>mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(GetHeightFromMapDensityFunction::mapInfo),
                    Codec.floatRange(DimensionType.MIN_Y * 2f, DimensionType.MAX_Y * 2f)
                            .fieldOf("lower_bound")
                            .forGetter(GetHeightFromMapDensityFunction::lowerBound),
                    Codec.floatRange(DimensionType.MIN_Y * 2f, DimensionType.MAX_Y * 2f)
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
        return 0;
    }

    @Override
    public double minValue() {
        return -1;
    }

    @Override
    public double maxValue() {
        return 1;
    }

    @Override
    public KeyDispatchDataCodec<? extends GetHeightFromMapDensityFunction> codec() {
        return CODEC;
    }
}