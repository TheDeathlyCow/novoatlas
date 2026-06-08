package com.thedeathlycow.novoatlas.impl.gen.interpolation;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.gen.MapImage;
import net.minecraft.util.Mth;

public final class Bicubic implements Interpolator {
    public static final MapCodec<Bicubic> CODEC = MapCodec.unit(new Bicubic());

    /// Implementation by [Paul Breeuwsma](https://www.paulinternet.nl/?page=bicubic)
    @Override
    public double sample(double x, double z, MapImage image) {
        int truncatedX = Mth.floor(x);
        int truncatedZ = Mth.floor(z);

        double deltaX = x - truncatedX;
        double deltaZ = z - truncatedZ;


        double[][] p = cubicNeighborhood(truncatedX, truncatedZ, image);

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

    @Override
    public MapCodec<Bicubic> codec() {
        return CODEC;
    }

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