package com.thedeathlycow.novoatlas.impl.gen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * @deprecated Replaced by {@link GetPreliminaryHeightFromMapDensityFunction} in 1.21.11+
 */
@Deprecated
public record InitialDensityHeightmapDensityFunction(
        Holder<MapInfo> mapInfo,
        double transitionRange
) implements DensityFunction.SimpleFunction {
    public static final MapCodec<InitialDensityHeightmapDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(InitialDensityHeightmapDensityFunction::mapInfo),
                    Codec.DOUBLE
                            .optionalFieldOf("transition_range", 10.0)
                            .forGetter(InitialDensityHeightmapDensityFunction::transitionRange)
            ).apply(instance, InitialDensityHeightmapDensityFunction::new)
    );

    public static final KeyDispatchDataCodec<InitialDensityHeightmapDensityFunction> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        int elevation = mapInfo.value().getHeightMapElevation(context.blockX(), context.blockZ()) - 8;

        int yOffset = elevation - context.blockY();

        return Mth.clampedMap(yOffset, -transitionRange, transitionRange, -1.0, 1.0);
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
    public KeyDispatchDataCodec<? extends InitialDensityHeightmapDensityFunction> codec() {
        return CODEC;
    }
}