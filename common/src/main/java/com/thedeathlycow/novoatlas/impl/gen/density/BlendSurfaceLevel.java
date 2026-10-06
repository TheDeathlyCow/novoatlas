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

public record BlendSurfaceLevel(
        Holder<MapInfo> mapInfo,
        DensityFunction insideMap,
        DensityFunction outsideMap,
        float blendDistance
) implements DensityFunction {
    public static final MapCodec<BlendSurfaceLevel> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(BlendSurfaceLevel::mapInfo),
                    DensityFunction.CODEC
                            .fieldOf("inside_map")
                            .forGetter(BlendSurfaceLevel::insideMap),
                    DensityFunction.CODEC
                            .fieldOf("outside_map")
                            .forGetter(BlendSurfaceLevel::outsideMap),
                    ExtraCodecs.POSITIVE_FLOAT
                            .fieldOf("blend_distance")
                            .forGetter(BlendSurfaceLevel::blendDistance)
            ).apply(instance, BlendSurfaceLevel::new)
    );

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        return new BlendSurfaceLevel.Sampler(
                this.mapInfo.value(),
                this.insideMap.compileSampler(context),
                this.outsideMap.compileSampler(context),
                this.blendDistance
        );
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction insideRewrite = rule.rewrite(this.insideMap);
        DensityFunction outsideRewrite = rule.rewrite(this.outsideMap);

        return insideRewrite != this.insideMap || outsideRewrite != this.outsideMap
                ? new BlendSurfaceLevel(this.mapInfo, insideRewrite, outsideRewrite, this.blendDistance)
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
    public MapCodec<BlendSurfaceLevel> codec() {
        return CODEC;
    }

    private record Sampler(
            MapInfo mapInfo,
            DensitySampler insideMap,
            DensitySampler outsideMap,
            float blendDistance
    ) implements BlendedSampler {
        @Override
        public void sampleBlendedVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume, HeightMapImage heightmap) {
            this.insideMap.sampleVolume(context, outputBuffer, volume);

            try (ScopedDensityBuffer outsideBuffer = context.acquireBuffer(volume)) {
                this.outsideMap.sampleVolume(context, outsideBuffer, volume);
                int index = 0;

                for (int z = 0; z < volume.sizeZ(); z++) {
                    for (int x = 0; x < volume.sizeX(); x++) {
                        final float alpha = BlendedSampler.smoothstep(
                                0,
                                this.blendDistance,
                                (float) heightmap.getDistanceToEdge(volume.blockX(x), volume.blockZ(z), this.mapInfo)
                        );

                        for (int y = 0; y < volume.sizeY(); y++) {
                            float outsideValue = outsideBuffer.get(index);
                            float insideValue = outputBuffer.get(index);
                            float blended3DValue = Mth.lerp(alpha, insideValue, outsideValue);

                            outputBuffer.set(index, blended3DValue);

                            index++;
                        }
                    }
                }
            }
        }

        @Override
        public float sampleBlendedValue(SamplerContext context, int blockX, int blockY, int blockZ, float alpha) {
            float inside = insideMap.sampleValue(context, blockX, blockY, blockZ);
            float outside = outsideMap.sampleValue(context, blockX, blockY, blockZ);
            return Mth.lerp(alpha, inside, outside);
        }
    }
}