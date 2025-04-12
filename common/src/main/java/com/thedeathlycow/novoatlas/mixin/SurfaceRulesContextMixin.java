package com.thedeathlycow.novoatlas.mixin;

import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import com.thedeathlycow.novoatlas.world.gen.condition.NovoAtlasSurfaceRulesContext;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SurfaceRules.Context.class)
public class SurfaceRulesContextMixin implements NovoAtlasSurfaceRulesContext {
    @Unique
    private MapInfo novoatlas$mapInfo = null;

    @Override
    public void novoatlas$setMapInfo(MapInfo mapInfo) {
        this.novoatlas$mapInfo = mapInfo;
    }

    @Override
    public @Nullable MapInfo novoatlas$getMapInfo() {
        return this.novoatlas$mapInfo;
    }
}