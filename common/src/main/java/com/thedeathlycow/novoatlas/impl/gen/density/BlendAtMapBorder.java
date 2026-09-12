package com.thedeathlycow.novoatlas.impl.gen.density;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public record BlendAtMapBorder(
        Holder<MapInfo> mapInfo,
        DensityFunction insideMap,
        DensityFunction outsideMap,
        float blendDistance
) implements DensityFunction {
    public static final MapCodec<BlendAtMapBorder> DATA_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(BlendAtMapBorder::mapInfo),
                    DensityFunction.CODEC
                            .fieldOf("inside_map")
                            .forGetter(BlendAtMapBorder::insideMap),
                    DensityFunction.CODEC
                            .fieldOf("outside_map")
                            .forGetter(BlendAtMapBorder::outsideMap),
                    ExtraCodecs.POSITIVE_FLOAT
                            .fieldOf("blend_distance")
                            .forGetter(BlendAtMapBorder::blendDistance)
            ).apply(instance, BlendAtMapBorder::new)
    );

    public static final KeyDispatchDataCodec<BlendAtMapBorder> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        double alpha = this.smoothstepDistance(context.blockX(), context.blockZ());

        if (alpha <= 0.0) {
            return outsideMap.compute(context);
        }

        if (alpha >= 1.0) {
            return insideMap.compute(context);
        }

        double inside = insideMap.compute(context);
        double outside = outsideMap.compute(context);
        return Mth.lerp(alpha, outside, inside);
    }

    private double smoothstepDistance(int x, int z) {
        double distance = mapInfo.value().getDistanceToEdge(x, z);
        return smoothstep(-blendDistance, blendDistance, distance);
    }

    /// Hermite Spline interpolation for better blending than simple lerp.
    ///
    /// Implementation is from [the Book of Shaders](https://thebookofshaders.com/glossary/?search=smoothstep).
    ///
    /// @param edge0 Lower edge
    /// @param edge1 Upper edge
    /// @param x Source value to interpolate
    private static double smoothstep(double edge0, double edge1, double x) {
        double t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    @Override
    public void fillArray(double[] output, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(output, this);
    }

    @Override
    public DensityFunction mapChildren(Visitor visitor) {
        return new BlendAtMapBorder(
                this.mapInfo,
                visitor.apply(this.insideMap),
                visitor.apply(this.outsideMap),
                this.blendDistance
        );
    }

    @Override
    public double minValue() {
        return Math.min(this.insideMap.minValue(), this.outsideMap.minValue());
    }

    @Override
    public double maxValue() {
        return Math.max(this.insideMap.maxValue(), this.outsideMap.maxValue());
    }

    @Override
    public KeyDispatchDataCodec<BlendAtMapBorder> codec() {
        return CODEC;
    }
}