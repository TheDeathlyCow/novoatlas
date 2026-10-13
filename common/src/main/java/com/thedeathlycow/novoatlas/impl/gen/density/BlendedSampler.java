package com.thedeathlycow.novoatlas.impl.gen.density;

import com.thedeathlycow.novoatlas.impl.image.HeightMapImage;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import com.thedeathlycow.novoatlas.impl.math.Blending;
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
        float alpha = Blending.smoothstep(0, this.blendDistance(), (float) this.mapInfo().getHeightmapDistanceToEdge(blockX, blockZ));

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
}