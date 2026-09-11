package com.thedeathlycow.novoatlas.mixin.accessor;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.stream.Stream;

@Mixin(BiomeSource.class)
public interface BiomeSourceAccessor {
    @Invoker("collectPossibleBiomes")
    Stream<Holder<Biome>> invokeCollectPossibleBiomes();
}
