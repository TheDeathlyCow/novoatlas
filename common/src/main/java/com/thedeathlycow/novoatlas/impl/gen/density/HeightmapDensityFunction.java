package com.thedeathlycow.novoatlas.impl.gen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.image.HeightMapImage;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.core.Holder;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.densityfunction.*;
import org.jspecify.annotations.NonNull;

/// Using a heightmap, computes positive density below the surface and negative density above the surface, with some
/// blending in between.
public record HeightmapDensityFunction(
        Holder<MapInfo> mapInfo,
        float transitionRange
) implements DensityFunction {
    public static final MapCodec<HeightmapDensityFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    MapInfo.CODEC
                            .fieldOf("map_info")
                            .forGetter(HeightmapDensityFunction::mapInfo),
                    Codec.FLOAT
                            .optionalFieldOf("transition_range", 10.0f)
                            .forGetter(HeightmapDensityFunction::transitionRange)
            ).apply(instance, HeightmapDensityFunction::new)
    );

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        return new Sampler(this.mapInfo.value(), this.transitionRange);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return this;
    }

    @Override
    @NonNull
    public Interval range() {
        return Interval.of(-1, 1);
    }

    @Override
    @Axes
    public int domainAxes() {
        return DensityFunction.ALL_AXES;
    }

    @Override
    public MapCodec<? extends HeightmapDensityFunction> codec() {
        return CODEC;
    }

    private record Sampler(MapInfo mapInfo, float transitionRange) implements DensitySampler {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            int index = 0;
            HeightMapImage heightmap = mapInfo.getHeightMap();

            for (int x = 0; x < volume.sizeX(); x++) {
                for (int y = 0; y < volume.sizeY(); y++) {
                    for (int z = 0; z < volume.sizeZ(); z++) {
                        int elevation = heightmap.sample(volume.blockX(x), volume.blockZ(z), mapInfo);
                        int yOffset = elevation - volume.blockY(y);
                        float density = Mth.clampedMap(yOffset, -transitionRange, transitionRange, -1.0f, 1.0f);
                        outputBuffer.addTo(index, density);

                        index++;
                    }
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            int elevation = mapInfo.getHeightMapElevation(blockX, blockZ);
            int yOffset = elevation - blockY;
            return Mth.clampedMap(yOffset, -transitionRange, transitionRange, -1.0f, 1.0f);
        }
    }
}