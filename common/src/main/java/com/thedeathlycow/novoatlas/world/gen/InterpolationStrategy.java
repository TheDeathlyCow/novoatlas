package com.thedeathlycow.novoatlas.world.gen;

import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

public enum InterpolationStrategy implements StringRepresentable {
    BILINEAR("bilinear") {
        @Override
        public double interpolate(double x, double z, MapImage image) {
            // x and z are the truncated (floor) coordinates
            // deltaX and deltaZ are the fractional parts
            int truncatedX = Mth.floor(x);
            int truncatedZ = Mth.floor(z);

            // x - truncatedX gets the fractional part of the sampled point, to use for lerp deltas
            double deltaX = x - truncatedX;
            double deltaZ = z - truncatedZ;

            int nextX = Math.min(truncatedX + 1, image.width() - 1);
            int nextZ = Math.min(truncatedZ + 1, image.height() - 1);

            int[][] pixels = image.pixels();

            int topLeft = pixels[truncatedX][truncatedZ];
            int topRight = pixels[nextX][truncatedZ];
            int bottomLeft = pixels[truncatedX][nextZ];
            int bottomRight = pixels[nextX][nextZ];

            return Mth.lerp2(deltaX, deltaZ, topLeft, topRight, bottomLeft, bottomRight);
        }
    },
    NEAREST_NEIGHBOR("nearest_neighbor") {
        @Override
        public double interpolate(double x, double z, MapImage image) {
            return image.getTruncated(x, z);
        }
    };

    public static final EnumCodec<InterpolationStrategy> CODEC = StringRepresentable.fromEnum(InterpolationStrategy::values);

    private final String name;

    InterpolationStrategy(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public abstract double interpolate(double x, double z, MapImage image);
}