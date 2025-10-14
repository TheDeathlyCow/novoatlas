package com.thedeathlycow.novoatlas.mixin.accessor;

import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

@Mixin(NoiseBasedChunkGenerator.class)
public interface NoiseBasedChunkGeneratorAccessor {
    @Accessor("globalFluidPicker")
    @Mutable
    void novoatlas$setGlobalFluidPicker(Supplier<Aquifer.FluidPicker> fluidPickerSupplier);
}
