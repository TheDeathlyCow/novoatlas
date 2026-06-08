package com.thedeathlycow.novoatlas.impl.gen.interpolation;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.impl.gen.MapImage;
import net.minecraft.util.ExtraCodecs;

/// Implementation based on [Lánczos interpolation explained](https://mazzo.li/posts/lanczos.html).
public record Lanczos(
        int window
) implements Interpolator {
    public static final MapCodec<Lanczos> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    ExtraCodecs.POSITIVE_INT
                            .optionalFieldOf("window", 3)
                            .forGetter(Lanczos::window)
            ).apply(instance, Lanczos::new)
    );

    @Override
    public double sample(double x, double z, MapImage image) {
        // x and z are the truncated (floor) coordinates
        // deltaX and deltaZ are the fractional parts
        double truncatedX = Math.floor(x);
        double truncatedZ = Math.floor(z);

        // x - truncatedX gets the fractional part of the sampled point, to use for lerp deltas
        double deltaX = x - truncatedX;
        double deltaZ = z - truncatedZ;

        double result = 0.0;
        double totalWeight = 0.0;

        // for loops provide a convolution
        for (int dx = -window; dx <= window; dx++) {
            for (int dz = -window; dz <= window; dz++) {
                double tX = Math.clamp(truncatedX + dx, 0, image.width() - 1.0);
                double tZ = Math.clamp(truncatedZ + dz, 0, image.height() - 1.0);

                // combine lanczos smoothing across x and z axes
                double smoothing = lanczosSmoothing1d(deltaX - dx) * lanczosSmoothing1d(deltaZ - dz);

                result += image.getTruncated(tX, tZ) * smoothing;
                totalWeight += smoothing;
            }
        }

        // normalizing for total weight seems to fix weird issues
        return totalWeight == 0 ? 0 : result / totalWeight;
    }

    @Override
    public MapCodec<Lanczos> codec() {
        return CODEC;
    }

    private double lanczosSmoothing1d(double t) {
        if (Math.abs(t) >= this.window) {
            return 0;
        }

        double piT = Math.PI * t;
        return sinc(piT) * sinc(piT / this.window);
    }

    private static double sinc(double x) {
        return x == 0 ? 1 : Math.sin(x) / x;
    }
}