package com.thedeathlycow.novoatlas.impl.v2.image.atlas.map;

import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.biome.ColorMapping;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ColorImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import com.thedeathlycow.novoatlas.impl.v2.image.sampler.ImageSampler;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.ImageTransform;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.Offset;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.Scaling;
import com.thedeathlycow.novoatlas.impl.v2.math.CoordinateSpace;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;

public record BiomeMap(
        ImageTransform transform,
        ImageSampler data,
        ColorMapping mapping
) {
    @Nullable
    public Holder<Biome> getBiome(
            int quartX,
            int quartY,
            int quartZ,
            ImageLoader<ColorImageData> loader,
            @Nullable Holder<Biome> surfaceBiome
    ) {
        Scaling scaling = this.transform().scaling();
        Offset offset = this.transform().offset();

        Scaling.InterpolatedScale horizontalScale = scaling.horizontalScale();
        float horizontalScaleValue = horizontalScale.value();
        Vector2fc centerOffset = offset.centerOffset();

        double xR = CoordinateSpace.coordToIndexWithAlpha(quartX, horizontalScaleValue, centerOffset.x(), this.data.width(loader));
        double zR = CoordinateSpace.coordToIndexWithAlpha(quartZ, horizontalScaleValue, centerOffset.y(), this.data.height(loader));

        int color = this.data.getTruncated(xR, zR, this.transform, loader);

        return this.mapping.getBiome(color, surfaceBiome);
    }
}