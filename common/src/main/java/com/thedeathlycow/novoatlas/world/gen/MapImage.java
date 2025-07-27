package com.thedeathlycow.novoatlas.world.gen;

import com.thedeathlycow.novoatlas.NovoAtlas;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MapImage {

    private static final int TILE_SIZE = 16;

    private final Path baseCacheDir;
    private final Type type;
    private final Map<Point, BufferedImage> tileCache = new HashMap<>();

    public MapImage(Path baseCacheDir, Type type) {
        this.baseCacheDir = baseCacheDir;
        this.type = type;
    }

    public int sample(int x, int z, MapInfo mapInfo, int fallback) {
        // Apply scaling
        x = (int) (x * mapInfo.horizontalScale());
        z = (int) (z * mapInfo.horizontalScale());

        int tileX = x / TILE_SIZE;
        int tileY = z / TILE_SIZE;
        int localX = x % TILE_SIZE;
        int localY = z % TILE_SIZE;

        Point tilePoint = new Point(tileX, tileY);
        BufferedImage tile = this.tileCache.get(tilePoint);

        if (tile == null) {
            Path tilePath = this.baseCacheDir
                    .resolve(String.valueOf(tileX))
                    .resolve(String.valueOf(tileY))
                    .resolve(this.type.name().toLowerCase() + ".png");
            if (!Files.exists(tilePath)) {
                return fallback;
            }
            try {
                tile = ImageIO.read(tilePath.toFile());
                this.tileCache.put(tilePoint, tile);
            } catch (IOException e) {
                NovoAtlas.LOGGER.error("Failed to read tile: {}", tilePath, e);
                return fallback;
            }
        }

        if (localX < 0 || localX >= tile.getWidth() || localY < 0 || localY >= tile.getHeight()) {
            return fallback;
        }

        int color = tile.getRGB(localX, localY);
        return this.type.getColor(color, mapInfo);
    }

    public int sample(int x, int z, MapInfo mapInfo) {
        return this.sample(x, z, mapInfo, 0);
    }

    public enum Type {
        HEIGHTMAP {
            @Override
            public int getColor(int color, @NotNull MapInfo mapInfo) {
                return (color & 0xFF) + mapInfo.startingY();
            }
        },
        BIOME_MAP {
            @Override
            public int getColor(int color, @NotNull MapInfo mapInfo) {
                return color;
            }
        };

        public abstract int getColor(int color, @NotNull MapInfo mapInfo);
    }

    private record Point(int x, int y) {}
}
