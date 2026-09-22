package com.thedeathlycow.novoatlas.impl.image.interpolation;

import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.image.ImageWrapping;
import com.thedeathlycow.novoatlas.impl.image.MapImage;
import com.thedeathlycow.novoatlas.impl.image.MapInfo;
import net.minecraft.util.Mth;

public final class Bicubic implements Interpolator {
    public static final MapCodec<Bicubic> CODEC = MapCodec.unit(new Bicubic());

    private static final int NEIGHBORHOOD_WIDTH = 4;

    /// Implementation by [Paul Breeuwsma](https://www.paulinternet.nl/?page=bicubic)
    @Override
    public double sample(double x, double z, MapImage image, MapInfo mapInfo) {
        int truncatedX = Mth.floor(x);
        int truncatedZ = Mth.floor(z);

        double deltaX = x - truncatedX;
        double deltaZ = z - truncatedZ;

        double[] neighborHood = new double[NEIGHBORHOOD_WIDTH * NEIGHBORHOOD_WIDTH];
        cubicNeighborhood(neighborHood, truncatedX, truncatedZ, image, mapInfo.imageWrapping());

        double[] arr = new double[NEIGHBORHOOD_WIDTH];
        arr[0] = getValue(neighborHood, 0, deltaZ);
        arr[1] = getValue(neighborHood, NEIGHBORHOOD_WIDTH, deltaZ);
        arr[2] = getValue(neighborHood, 2 * NEIGHBORHOOD_WIDTH, deltaZ);
        arr[3] = getValue(neighborHood, 3 * NEIGHBORHOOD_WIDTH, deltaZ);
        return getValue(arr, 0, deltaX);
    }

    private static double getValue(double[] p, int offset, double x) {
        double p0 = p[offset];
        double p1 = p[offset + 1];
        double p2 = p[offset + 2];
        double p3 = p[offset + 3];

        return p1 + 0.5 * x * (p2 - p0 + x * (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3 + x * (3.0 * (p1 - p2) + p3 - p0)));
    }

    @Override
    public MapCodec<Bicubic> codec() {
        return CODEC;
    }

    private static void cubicNeighborhood(double[] output, int x, int z, MapImage image, ImageWrapping imageWrapping) {
        for (int col = -1; col < 3; col++) {
            for (int row = -1; row < 3; row++) {
                int px = x + col;
                int pz = z + row;
                output[(col + 1) * NEIGHBORHOOD_WIDTH + row + 1] = image.getPixelValue(px, pz, imageWrapping);
            }
        }
    }
}