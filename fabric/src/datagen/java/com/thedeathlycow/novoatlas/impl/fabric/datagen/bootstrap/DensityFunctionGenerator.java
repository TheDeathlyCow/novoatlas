package com.thedeathlycow.novoatlas.impl.fabric.datagen.bootstrap;

import com.thedeathlycow.novoatlas.impl.gen.NovoAtlasDensityFunctions;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;


public final class DensityFunctionGenerator {
    public static void bootstrap(BootstrapContext<DensityFunction> context) {
        final HolderGetter<DensityFunction> functions = context.lookup(Registries.DENSITY_FUNCTION);
        final HolderGetter<NormalNoise> noises = context.lookup(Registries.NOISE);

        registerCaves(context, functions, noises, NovoAtlasDensityFunctions.CAVES);
        registerCaves(context, functions, noises, NovoAtlasDensityFunctions.NO_CAVE_ENTRANCES);

        context.register(NovoAtlasDensityFunctions.NO_CAVES, DensityFunctions.constant(0.1f));
    }

    private static void registerCaves(
            final BootstrapContext<DensityFunction> context,
            final HolderGetter<DensityFunction> functions,
            final HolderGetter<NormalNoise> noises,
            final ResourceKey<DensityFunction> name
    ) {
        DensityFunction caves = NoiseRouterData.underground(functions, noises, DensityFunctions.constant(0.1f));

        context.register(
                name,
                DensityFunctions.min(
                        NoiseRouterData.postProcess(NoiseRouterData.slideOverworld(false, caves), 4, 8),
                        NoiseRouterData.getFunction(functions, NoiseRouterData.NOODLE)
                )
        );
    }

    private DensityFunctionGenerator() {

    }
}