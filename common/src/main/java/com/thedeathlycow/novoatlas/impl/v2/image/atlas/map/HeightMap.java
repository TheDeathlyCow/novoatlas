package com.thedeathlycow.novoatlas.impl.v2.image.atlas.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.v2.image.data.GrayscaleImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.interpolation.Interpolator;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import com.thedeathlycow.novoatlas.impl.v2.image.sampler.ImageSampler;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.ImageTransform;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.Offset;
import com.thedeathlycow.novoatlas.impl.v2.image.transform.Scaling;
import com.thedeathlycow.novoatlas.impl.v2.math.CoordinateSpace;
import com.thedeathlycow.novoatlas.impl.v2.registry.NovoAtlasRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.codec.RegistryFileCodec;
import org.joml.Vector2fc;

public final class HeightMap {
    public static final Codec<HeightMap> DIRECT_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ImageTransform.CODEC
                            .optionalFieldOf("transform", ImageTransform.DEFAULT)
                            .forGetter(i -> i.transform),
                    ImageSampler.BASE_CODEC
                            .fieldOf("data")
                            .forGetter(i -> i.data)
            ).apply(instance, HeightMap::new)
    );

    public static final Codec<Holder<HeightMap>> CODEC = RegistryFileCodec.create(NovoAtlasRegistries.HEIGHT_MAP, DIRECT_CODEC, false);

    private final ImageTransform transform;

    private final ImageSampler data;

    public HeightMap(ImageTransform transform, ImageSampler data) {
        this.transform = transform;
        this.data = data;
    }

    public int getHeightValue(int coordX, int coordZ, ImageLoader<GrayscaleImageData> loader) {
        Scaling scaling = this.transform.scaling();
        Offset offset = this.transform.offset();

        Scaling.InterpolatedScale horizontalScale = scaling.horizontalScale();
        float horizontalScaleValue = horizontalScale.value();
        Vector2fc centerOffset = offset.centerOffset();

        double xR = CoordinateSpace.coordToIndexWithAlpha(coordX, horizontalScaleValue, centerOffset.x(), this.data.width(loader));
        double zR = CoordinateSpace.coordToIndexWithAlpha(coordZ, horizontalScaleValue, centerOffset.y(), this.data.height(loader));

        double height = horizontalScale.interpolation().sample(xR, zR, Interpolator.context(this.data, this.transform, loader));

        return (int) Math.round(scaling.verticalScale() * height + offset.startingY());
    }
}