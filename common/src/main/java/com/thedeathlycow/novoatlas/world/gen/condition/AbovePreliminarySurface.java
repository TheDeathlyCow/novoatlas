package com.thedeathlycow.novoatlas.world.gen.condition;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thedeathlycow.novoatlas.mixin.accessor.SurfaceRulesContextAccessor;
import com.thedeathlycow.novoatlas.world.gen.MapInfo;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record AbovePreliminarySurface(
        int depth
) implements SurfaceRules.ConditionSource {
    public static final KeyDispatchDataCodec<AbovePreliminarySurface> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                    instance -> instance.group(
                            ExtraCodecs.intRange(1, 10)
                                    .fieldOf("depth")
                                    .forGetter(AbovePreliminarySurface::depth)
                    ).apply(instance, AbovePreliminarySurface::new)
            )
    );

    @Override
    public KeyDispatchDataCodec<AbovePreliminarySurface> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.Condition apply(SurfaceRules.Context context) {
        return () -> {
            MapInfo mapInfo = ((NovoAtlasSurfaceRulesContext) context).novoatlas$getMapInfo();

            // should only apply when carvers call this function, which is okay to always have grass
            if (mapInfo == null) {
                return true;
            }

            SurfaceRulesContextAccessor accessor = (SurfaceRulesContextAccessor) context;

            double elevation = mapInfo.getHeightMapElevation(
                    accessor.blockX(),
                    accessor.blockZ()
            );

            return accessor.blockY() > elevation - this.depth;
        };
    }
}