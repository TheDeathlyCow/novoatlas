package com.thedeathlycow.novoatlas.world.gen;

import net.minecraft.util.Mth;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public record MapImage(
        int width,
        int height,
        int[][] pixels,
        Type type
) {
    public enum Type {
        BIOME_MAP,
        HEIGHTMAP
    }

    public static MapImage fromBufferedImage(BufferedImage image, Type type) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[][] pixels = type == Type.BIOME_MAP ? getColorPixels(image, width, height) : getGrayScalePixels(image, width, height);

        return new MapImage(width, height, pixels, type);
    }

    private static int[][] getGrayScalePixels(BufferedImage image, int width, int height) {
        int[][] pixels = new int[width][height];
        Raster raster = image.getRaster();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[x][y] = raster.getSample(x, y, 0);
            }
        }

        return pixels;
    }

    private static int[][] getColorPixels(BufferedImage image, int width, int height) {
        int[] data = new int[width * height];
        image.getRGB(0, 0, width, height, data, 0, width);

        int x = 0;
        int y = 0;
        int[][] pixels = new int[width][height];

        for (int datum : data) {
            if (x >= width) {
                x = 0;
                y++;
            }
            pixels[x++][y] = datum & 0xffffff;
        }

        return pixels;
    }

    public int sample(int x, int z, MapInfo info) {
        return this.sample(x, z, info, Integer.MIN_VALUE);
    }

    public int sample(int x, int z, MapInfo info, int fallback) {
        float xR = (x / info.horizontalScale()) + this.width() / 2f; // these will always be even numbers
        float zR = (z / info.horizontalScale()) + this.height() / 2f;

        if (xR < 0 || zR < 0 || xR >= this.width() || zR >= this.height()) {
            return fallback;
        }

        int truncatedX = Mth.floor(xR);
        int truncatedZ = Mth.floor(zR);

        if (this.type == Type.HEIGHTMAP) {
            double height = this.bilerp(truncatedX, xR - truncatedX, truncatedZ, zR - truncatedZ);

            return Mth.floor(info.verticalScale() * height + info.startingY());
        } else {
            return this.pixels[truncatedX][truncatedZ];
        }
    }

    private double bilerp(int truncatedX, float xR, int truncatedZ, float zR) {
        int dx = 0;
        int dz = 0;

        int u0 = Math.max(0, truncatedX + dx);
        int v0 = Math.max(0, truncatedZ + dz);

        int u1 = Math.min(width - 1, u0 + 1);
        int v1 = Math.min(v0 + 1, height - 1);

        float i00 = pixels[u0][v0];
        float i01 = pixels[u1][v0];
        float i10 = pixels[u0][v1];
        float i11 = pixels[u1][v1];

        return Mth.lerp2(Math.abs(xR), Math.abs(zR), i00, i10, i01, i11);
    }
}