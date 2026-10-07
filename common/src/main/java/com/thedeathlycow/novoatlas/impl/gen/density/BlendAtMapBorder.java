package com.thedeathlycow.novoatlas.impl.gen.density;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.image.HeightMapImage;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.*;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public record BlendAtMapBorder(
        Holder<MapInfo> mapInfo,
        DensityFunction preliminaryHeight,
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
                            .fieldOf("preliminary_height")
                            .forGetter(BlendAtMapBorder::preliminaryHeight),
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
                this.preliminaryHeight.compileSampler(context),
                this.insideMap.compileSampler(context),
                this.outsideMap.compileSampler(context),
                this.blendDistance
        );
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction preliminaryHeight = rule.rewrite(this.preliminaryHeight);
        DensityFunction insideRewrite = rule.rewrite(this.insideMap);
        DensityFunction outsideRewrite = rule.rewrite(this.outsideMap);

        return insideRewrite != this.insideMap || outsideRewrite != this.outsideMap || preliminaryHeight != this.preliminaryHeight
                ? new BlendAtMapBorder(this.mapInfo, preliminaryHeight, insideRewrite, outsideRewrite, this.blendDistance)
                : this;
    }

    @Override
    public Interval range() {
        return Interval.encapsulating(this.insideMap.range(), this.outsideMap.range());
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
            DensitySampler preliminaryHeight,
            DensitySampler insideMap,
            DensitySampler outsideMap,
            float blendDistance
    ) implements BlendedSampler {
        @Override
        public void sampleBlendedVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume, HeightMapImage heightMap) {
            this.preliminaryHeight.sampleVolume(context, outputBuffer, volume);

            try (
                    ScopedDensityBuffer insideBuffer = context.acquireBuffer(volume);
                    ScopedDensityBuffer outsideBuffer = context.acquireBuffer(volume)
            ) {
                this.insideMap.sampleVolume(context, insideBuffer, volume);
                this.outsideMap.sampleVolume(context, outsideBuffer, volume);
                int index = 0;

                for (int z = 0; z < volume.sizeZ(); z++) {
                    for (int x = 0; x < volume.sizeX(); x++) {
                        final float alpha = BlendedSampler.smoothstep(
                                0,
                                this.blendDistance,
                                (float) heightMap.getDistanceToEdge(volume.blockX(x), volume.blockZ(z), this.mapInfo)
                        );
                        final float tapering = taperWeight(alpha);

                        for (int y = 0; y < volume.sizeY(); y++) {
                            float height = outputBuffer.get(index) + 8;
                            float yOffset = Mth.clamp(height - volume.blockY(y), -5, 5);
                            float heightDensity = yOffset / 5;

                            float outsideValue = outsideBuffer.get(index);
                            float insideValue = insideBuffer.get(index);
                            float blended3DValue = Mth.lerp(alpha, insideValue, outsideValue);

                            outputBuffer.set(index, tapering * heightDensity + blended3DValue);

                            index++;
                        }
                    }
                }
            }
        }

        @Override
        public float sampleBlendedValue(SamplerContext context, int blockX, int blockY, int blockZ, float alpha) {
            float elevation = this.preliminaryHeight.sampleValue(context, blockX, blockY, blockZ) + 8;
            float yOffset = Mth.clamp(elevation - blockY, -5, 5);
            float heightDensity = yOffset / 5;

            float inside = insideMap.sampleValue(context, blockX, blockY, blockZ);
            float outside = outsideMap.sampleValue(context, blockX, blockY, blockZ);
            return taperWeight(alpha) * heightDensity + Mth.lerp(alpha, inside, outside);
        }

        private static float taperWeight(float alpha) {
            return 4.0f * alpha * (1.0f - alpha);
        }
    }
}