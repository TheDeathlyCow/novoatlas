package com.thedeathlycow.novoatlas.impl.gen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public record InitialDensityHeightmapDF(
        Holder<MapInfo> mapInfo,
        double transitionRange
) implements DensityFunction.SimpleFunction {
    public static final MapCodec<InitialDensityHeightmapDF> DATA_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(InitialDensityHeightmapDF::mapInfo),
                    Codec.DOUBLE
                            .optionalFieldOf("transition_range", 10.0)
                            .forGetter(InitialDensityHeightmapDF::transitionRange)
            ).apply(instance, InitialDensityHeightmapDF::new)
    );

    public static final KeyDispatchDataCodec<InitialDensityHeightmapDF> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

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
    public KeyDispatchDataCodec<? extends InitialDensityHeightmapDF> codec() {
        return CODEC;
    }
}