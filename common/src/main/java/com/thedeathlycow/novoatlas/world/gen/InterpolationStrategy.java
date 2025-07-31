package com.thedeathlycow.novoatlas.world.gen;

import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4d;

public enum InterpolationStrategy implements StringRepresentable {
    BILINEAR("bilinear") {
        @Override
        public double interpolate(double x, double z, MapImage image, MapScaleConfig.LanczosConfig lanczosConfig) {
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
        public double interpolate(double x, double z, MapImage image, MapScaleConfig.LanczosConfig lanczosConfig) {
            return image.getTruncated(x, z);
        }
    },
    BICUBIC("bicubic") {
        /**
         * Implementation by <a href="https://www.paulinternet.nl/?page=bicubic">Paul Breeuwsma</a>
         */
        @Override
        public double interpolate(double x, double z, MapImage image, MapScaleConfig.LanczosConfig lanczosConfig) {
            // yeah this is horrible, sorry
            // based on algorithm described here: https://en.wikipedia.org/wiki/Bicubic_interpolation
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
    },
    LANCZOS("lanczos") {

        /**
         * Sinc function: sin(πx)/(πx)
         */
        private static float sinc(double x) {
            if (x == 0.0) {
                return 1;
            }
            return Mth.sin((float) (Mth.PI * x)) / (float) (Mth.PI * x);
        }

        /**
         * Lanczos kernel function: L(x) = sinc(x) * sinc(x/a) for -a < x < a, 0 otherwise
         */
        private static double lanczosKernel(double x, int a) {
            if ((-a < x) && (x < a)) {
                return sinc(x) * sinc(x / a);
            }
            return 0.0;
        }

        /**
         * Get pixel value with boundary handling
         */
        private static double getPixelValue(int x, int z, MapImage image, boolean clampToEdge) {
            int[][] pixels = image.pixels();
            int width = image.width();
            int height = image.height();

            if (clampToEdge) {
                // Clamp-to-edge outside bounds
                x = Mth.clamp(x, 0, width - 1);
                z = Mth.clamp(z, 0, height - 1);
                return pixels[x][z];
            } else {
                // Zero outside bounds
                if ((x < 0) || (x >= width) || (z < 0) || (z >= height)) {
                    return 0.0;
                }
                return pixels[x][z];
            }
        }

        @Override
        public double interpolate(double x, double z, MapImage image, MapScaleConfig.LanczosConfig lanczosConfig) {
            // Get the floor coordinates
            int floorX = Mth.floor(x);
            int floorZ = Mth.floor(z);

            double result = 0.0;
            double weightSum = 0.0;

            int kernelSize = lanczosConfig.kernelSize();
            boolean clampToEdge = lanczosConfig.clampToEdge();

            // Apply Lanczos kernel in both X and Z directions
            for (int i = -kernelSize + 1; i <= kernelSize; i++) {
                for (int j = -kernelSize + 1; j <= kernelSize; j++) {
                    // Calculate the kernel weights
                    double kernelX = lanczosKernel(i - (x - floorX), kernelSize);
                    double kernelZ = lanczosKernel(j - (z - floorZ), kernelSize);
                    double weight = kernelX * kernelZ;

                    // Get the pixel value at the sample point
                    double pixelValue = getPixelValue(floorX + i, floorZ + j, image, clampToEdge);

                    // Accumulate the weighted sum
                    result += pixelValue * weight;
                    weightSum += weight;
                }
            }

            // Normalize by the sum of weights to prevent brightness changes
            if (weightSum != 0.0) {
                result /= weightSum;
            }

            return result;
        }
    };

    public static final EnumCodec<InterpolationStrategy> CODEC = StringRepresentable.fromEnum(InterpolationStrategy::values);

    private final String name;

    private static final Matrix4d HERMITE_SPLINE = new Matrix4d(
            1, 0, 0, 0,
            0, 0, 1, 0,
            -3, 3, -2, -1,
            2, -2, 1, 1
    );

    InterpolationStrategy(String name) {
        this.name = name;
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return this.name;
    }

    public abstract double interpolate(double x, double z, MapImage image, MapScaleConfig.LanczosConfig lanczosConfig);

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
