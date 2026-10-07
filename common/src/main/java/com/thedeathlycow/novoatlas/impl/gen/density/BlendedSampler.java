package com.thedeathlycow.novoatlas.impl.gen.density;

import com.thedeathlycow.novoatlas.impl.image.HeightMapImage;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

public interface BlendedSampler extends DensitySampler {
    MapInfo mapInfo();

    DensitySampler insideMap();

    DensitySampler outsideMap();

    float blendDistance();

    @Override
    default void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
        HeightMapImage heightmap = this.mapInfo().getHeightMap();

        // if the volume does not overlap the transition area then just use the outside map function
        // this includes the inside of the map!
        if (!this.volumeOverlaps(heightmap, volume, Mth.ceil(this.blendDistance()))) {
            this.outsideMap().sampleVolume(context, outputBuffer, volume);
            return;
        }
        // if the volume is entirely contained in the interior image then just use the inside map function
        if (this.volumeInside(heightmap, volume)) {
            this.insideMap().sampleVolume(context, outputBuffer, volume);
            return;
        }

        this.sampleBlendedVolume(context, outputBuffer, volume, heightmap);
    }

    @Override
    default float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
        float alpha = smoothstep(0, this.blendDistance(), (float) this.mapInfo().getHeightmapDistanceToEdge(blockX, blockZ));

        if (alpha >= 1.0) {
            return this.outsideMap().sampleValue(context, blockX, blockY, blockZ);
        }

        if (alpha <= 0) {
            return this.insideMap().sampleValue(context, blockX, blockY, blockZ);
        }

        return this.sampleBlendedValue(context, blockX, blockY, blockZ, alpha);
    }

    void sampleBlendedVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume, HeightMapImage heightmap);

    float sampleBlendedValue(SamplerContext context, int blockX, int blockY, int blockZ, float alpha);

    private boolean volumeOverlaps(HeightMapImage image, DensityVolume volume, int buffer) {
        MapInfo mapInfo = this.mapInfo();
        return volume.minBlockX() <= image.maxBlockX(mapInfo) + buffer
                && volume.maxBlockX() >= image.minBlockX(mapInfo) - buffer
                && volume.minBlockZ() <= image.maxBlockZ(mapInfo) + buffer
                && volume.maxBlockZ() >= image.minBlockZ(mapInfo) - buffer;
    }

    private boolean volumeInside(HeightMapImage image, DensityVolume volume) {
        MapInfo mapInfo = this.mapInfo();
        return volume.minBlockX() >= image.minBlockX(mapInfo)
                && volume.maxBlockX() <= image.maxBlockX(mapInfo)
                && volume.minBlockZ() >= image.minBlockZ(mapInfo)
                && volume.maxBlockZ() <= image.maxBlockZ(mapInfo);
    }

    /// Hermite Spline interpolation for better blending than simple lerp.
    ///
    /// Implementation is from [the Book of Shaders](https://thebookofshaders.com/glossary/?search=smoothstep).
    ///
    /// @param edge0 Lower edge
    /// @param edge1 Upper edge
    /// @param x     Source value to interpolate
    static float smoothstep(float edge0, float edge1, float x) {
        float t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }
}