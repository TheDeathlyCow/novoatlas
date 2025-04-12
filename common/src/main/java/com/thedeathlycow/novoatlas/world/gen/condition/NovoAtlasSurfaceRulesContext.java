package com.thedeathlycow.novoatlas.world.gen.condition;

import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import org.jetbrains.annotations.Nullable;

public interface NovoAtlasSurfaceRulesContext {
    void novoatlas$setMapInfo(MapInfo mapInfo);

    @Nullable
    MapInfo novoatlas$getMapInfo();
}
