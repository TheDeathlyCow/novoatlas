package com.thedeathlycow.novoatlas.world.gen;

import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum InterpolationStrategy implements StringRepresentable {
    NEAREST_NEIGHBOR("nearest_neighbor") {
        @Override
        public double interpolate(double x, double z, MapImage image) {
            return image.getTruncated(x, z);
        }
    },
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
    BICUBIC("bicubic") {
        /**
         * Implementation by <a href="https://www.paulinternet.nl/?page=bicubic">Paul Breeuwsma</a>
         */
        @Override
        public double interpolate(double x, double z, MapImage image) {
            int truncatedX = Mth.floor(x);
            int truncatedZ = Mth.floor(z);

            double deltaX = x - truncatedX;
            double deltaZ = z - truncatedZ;


            double[][] p = InterpolationStrategy.cubicNeighborhood(truncatedX, truncatedZ, image);

            double[] arr = new double[4];
            arr[0] = getValue(p[0], deltaZ);
            arr[1] = getValue(p[1], deltaZ);
            arr[2] = getValue(p[2], deltaZ);
            arr[3] = getValue(p[3], deltaZ);
            return getValue(arr, deltaX);
        }

        private static double getValue(double[] p, double x) {
            return p[1] + 0.5 * x * (p[2] - p[0] + x * (2.0 * p[0] - 5.0 * p[1] + 4.0 * p[2] - p[3] + x * (3.0 * (p[1] - p[2]) + p[3] - p[0])));
        }
    };

    public static final EnumCodec<InterpolationStrategy> CODEC = StringRepresentable.fromEnum(InterpolationStrategy::values);

    private final String name;

    InterpolationStrategy(String name) {
        this.name = name;
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return this.name;
    }

    public abstract double interpolate(double x, double z, MapImage image);

    private static double[][] cubicNeighborhood(int x, int z, MapImage image) {
        int[][] pixels = image.pixels();
        int width = image.width();
        int height = image.height();

        double[][] G = new double[4][4];

        for (int col = -1; col < 3; col++) {
            for (int row = -1; row < 3; row++) {
                int px = Mth.clamp(x + col, 0, width - 1);
                int pz = Mth.clamp(z + row, 0, height - 1);
                G[col + 1][row + 1] = pixels[px][pz];
            }
        }

        return G;
    }
}