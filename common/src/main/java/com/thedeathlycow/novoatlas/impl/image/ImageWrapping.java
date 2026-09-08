package com.thedeathlycow.novoatlas.impl.image;

import com.mojang.serialization.Codec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum ImageWrapping implements StringRepresentable {
    CLAMP_TO_EDGE("clamp_to_edge"),
    REPEAT("repeat"),
    MIRRORED_REPEAT("mirrored_repeat");

    private final String name;

    ImageWrapping(String name) {
        this.name = name;
    }

    public static final Codec<ImageWrapping> CODEC = StringRepresentable.fromValues(ImageWrapping::values);

    @FunctionalInterface
    public interface ValueGetter {
        int get(int x, int z);
    }

    public int transform(int x, int z, int width, int height, ValueGetter pixels) {
        return switch (this) {
            case CLAMP_TO_EDGE -> pixels.get(Mth.clamp(x, 0, width - 1), Mth.clamp(z, 0, height - 1));
            case REPEAT -> pixels.get(Math.floorMod(x, width),  Math.floorMod(z, height));
            case MIRRORED_REPEAT -> pixels.get(mirror(x, width), mirror(z, height));
        };
    }

    private static int mirror(int coord, int size) {
        int period = size * 2;
        int m = Math.floorMod(coord, period);
        return m < size ? m : period - 1 - m;
    }

    @Override
    @NonNull
    public String getSerializedName() {
        return this.name;
    }
}