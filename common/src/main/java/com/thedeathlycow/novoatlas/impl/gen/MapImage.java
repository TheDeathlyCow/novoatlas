package com.thedeathlycow.novoatlas.impl.gen;

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
        float horizontalScale = info.horizontalScale().value();
        double xR = (x / horizontalScale) + this.width() / 2.0; // these will always be even numbers
        double zR = (z / horizontalScale) + this.height() / 2.0;

        if (xR < 0 || zR < 0 || xR >= this.width() || zR >= this.height()) {
            return fallback;
        }

        return this.sampleDirect(xR, zR, info);
    }

    private int sampleDirect(double x, double z, MapInfo info) {
        if (this.type == Type.HEIGHTMAP) {
            double height = info.horizontalScale().sample(x, z, this);
            return (int) Math.round(info.verticalScale() * height + info.startingY());
        } else {
            return this.getTruncated(x, z);
        }
    }

    public int getTruncated(double x, double z) {
        int truncatedX = Mth.floor(x);
        int truncatedZ = Mth.floor(z);
        return this.pixels[truncatedX][truncatedZ];
    }
}