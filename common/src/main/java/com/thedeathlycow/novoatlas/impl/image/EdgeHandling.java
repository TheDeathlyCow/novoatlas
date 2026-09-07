package com.thedeathlycow.novoatlas.impl.image;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.thedeathlycow.novoatlas.impl.registry.NovoAtlasBuiltinRegistries;
import net.minecraft.util.Mth;

import java.util.function.Function;

public interface EdgeHandling {
    MapCodec<EdgeHandling> BASE_CODEC = NovoAtlasBuiltinRegistries.EDGE_HANDLING.byNameCodec()
            .dispatchMap(
                    "mode",
                    EdgeHandling::codec,
                    Function.identity()
            );

    int transform(int x, int z, int width, int height, ValueGetter pixels);

    MapCodec<? extends EdgeHandling> codec();

    @FunctionalInterface
    interface ValueGetter {
        int getPixelValue(int x, int z);
    }

    final class ClampToEdge implements EdgeHandling {
        public static final MapCodec<ClampToEdge> CODEC = MapCodec.unit(new ClampToEdge());

        @Override
        public int transform(int x, int z, int width, int height, ValueGetter pixels) {
            return pixels.getPixelValue(Mth.clamp(x, 0, width - 1), Mth.clamp(z, 0, height - 1));
        }

        @Override
        public MapCodec<ClampToEdge> codec() {
            return CODEC;
        }
    }

    record FixedValue(int value) implements EdgeHandling {
        public static final MapCodec<FixedValue> CODEC = Codec.INT.fieldOf("value").xmap(FixedValue::new, FixedValue::value);

        @Override
        public int transform(int x, int z, int width, int height, ValueGetter pixels) {
            if (x < 0 || z < 0 || x >= width || z >= height) {
                return this.value;
            }

            return pixels.getPixelValue(x, z);
        }

        @Override
        public MapCodec<FixedValue> codec() {
            return CODEC;
        }
    }

    final class Repeat implements EdgeHandling {
        public static final MapCodec<Repeat> CODEC = MapCodec.unit(new Repeat());

        @Override
        public int transform(int x, int z, int width, int height, ValueGetter pixels) {
            return pixels.getPixelValue(Math.floorMod(x, width),  Math.floorMod(z, height));
        }

        @Override
        public MapCodec<Repeat> codec() {
            return CODEC;
        }
    }

    final class MirroredRepeat implements EdgeHandling {
        public static final MapCodec<MirroredRepeat> CODEC = MapCodec.unit(new MirroredRepeat());

        @Override
        public int transform(int x, int z, int width, int height, ValueGetter pixels) {
            return pixels.getPixelValue(mirror(x, width), mirror(z, height));
        }

        @Override
        public MapCodec<MirroredRepeat> codec() {
            return CODEC;
        }

        private static int mirror(int coord, int size) {
            int period = size * 2;
            int m = Math.floorMod(coord, period); // always in [0, period)
            return m < size ? m : period - 1 - m;
        }
    }
}