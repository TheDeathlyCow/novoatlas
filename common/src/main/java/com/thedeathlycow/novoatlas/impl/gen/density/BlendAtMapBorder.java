package com.thedeathlycow.novoatlas.impl.gen.density;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.image.HeightMapImage;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.densityfunction.*;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public record BlendAtMapBorder(
        Holder<MapInfo> mapInfo,
        DensityFunction insideMap,
        DensityFunction outsideMap,
        float blendDistance
) implements DensityFunction {
    public static final MapCodec<BlendAtMapBorder> CODEC = RecordCodecBuilder.mapCodec(
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

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        return new Sampler(
                this.mapInfo.value(),
                this.insideMap.compileSampler(context),
                this.outsideMap.compileSampler(context),
                this.blendDistance
        );
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction insideRewrite = this.insideMap.rewriteChildren(rule);
        DensityFunction outsideRewrite = this.outsideMap.rewriteChildren(rule);

        return insideRewrite != this.insideMap || outsideRewrite != this.outsideMap
                ? new BlendAtMapBorder(this.mapInfo, insideRewrite, outsideRewrite, this.blendDistance)
                : this;
    }

    @Override
    public Interval range() {
        return Interval.sub(this.insideMap.range(), this.outsideMap.range());
    }

    @Override
    public @Axes int domainAxes() {
        return DensityFunction.ALL_AXES;
    }

    @Override
    public MapCodec<BlendAtMapBorder> codec() {
        return CODEC;
    }

    private record Sampler(
            MapInfo mapInfo,
            DensitySampler insideMap,
            DensitySampler outsideMap,
            float blendDistance
    ) implements DensitySampler {

        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            HeightMapImage heightmap = mapInfo.getHeightMap();
            this.insideMap.sampleVolume(context, outputBuffer, volume);

            try (ScopedDensityBuffer outsideBuffer = context.acquireBuffer(volume)) {
                this.outsideMap.sampleVolume(context, outsideBuffer, volume);
                int index = 0;

                for (int z = 0; z < volume.sizeZ(); z++) {
                    for (int x = 0; x < volume.sizeX(); x++) {
                        final float alpha = this.smoothstepDistance(heightmap.getDistanceToEdge(volume.blockX(x), volume.blockZ(z), this.mapInfo));

                        if (alpha <= 0.0) {
                            for (int y = 0; y < volume.sizeY(); y++) {
                                outputBuffer.set(index, outsideBuffer.get(index));
                                index++;
                            }
                        } else if (alpha <= 1.0) {
                            for (int y = 0; y < volume.sizeY(); y++) {
                                float outsideValue = outsideBuffer.get(index);
                                float insideValue = outputBuffer.get(index);

                                float blendedValue = Mth.lerp(alpha, outsideValue, insideValue);
                                outputBuffer.set(index, blendedValue);

                                index++;
                            }
                        } else {
                            // dont need to update the output buffer as it already holds the desired inside value
                            index += volume.sizeY();
                        }
                    }
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float alpha = this.smoothstepDistance(mapInfo.getDistanceToEdge(blockX, blockZ));

            if (alpha <= 0.0) {
                return outsideMap.sampleValue(context, blockX, blockY, blockZ);
            }

            if (alpha >= 1.0) {
                return insideMap.sampleValue(context, blockX, blockY, blockZ);
            }

            float inside = insideMap.sampleValue(context, blockX, blockY, blockZ);
            float outside = outsideMap.sampleValue(context, blockX, blockY, blockZ);
            return Mth.lerp(alpha, outside, inside);
        }

        private float smoothstepDistance(float distance) {
            return smoothstep(-blendDistance, blendDistance, distance);
        }

        /// Hermite Spline interpolation for better blending than simple lerp.
        ///
        /// Implementation is from [the Book of Shaders](https://thebookofshaders.com/glossary/?search=smoothstep).
        ///
        /// @param edge0 Lower edge
        /// @param edge1 Upper edge
        /// @param x     Source value to interpolate
        private static float smoothstep(float edge0, float edge1, float x) {
            float t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
            return t * t * (3.0f - 2.0f * t);
        }
    }
}