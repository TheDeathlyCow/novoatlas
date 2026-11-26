package com.thedeathlycow.novoatlas.impl.v2.image.atlas;

import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.BiomeMap;
import com.thedeathlycow.novoatlas.impl.v2.image.atlas.map.HeightMap;

public class Atlas {
    private final HeightMap heightMap;
    private final BiomeMap surfaceBiomeMap;

    public Atlas(HeightMap heightMap, BiomeMap surfaceBiomeMap) {
        this.heightMap = heightMap;
        this.surfaceBiomeMap = surfaceBiomeMap;
    }
}