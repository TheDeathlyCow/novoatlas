package com.thedeathlycow.novoatlas.impl.fabric.datagen.bootstrap;

import com.thedeathlycow.novoatlas.impl.gen.NovoAtlasDensityFunctions;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.OverworldFunctionSet;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;


public final class DensityFunctionGenerator {
    public static void bootstrap(BootstrapContext<DensityFunction> context) {
        final HolderGetter<DensityFunction> functions = context.lookup(Registries.DENSITY_FUNCTION);
        final HolderGetter<NormalNoise> noises = context.lookup(Registries.NOISE);

        registerCaves(context, functions, noises, NovoAtlasDensityFunctions.CAVES, true);
        registerCaves(context, functions, noises, NovoAtlasDensityFunctions.NO_CAVE_ENTRANCES, false);

        context.register(NovoAtlasDensityFunctions.NO_CAVES, DensityFunctions.constant(0.1f));
    }

    private static void registerCaves(
            final BootstrapContext<DensityFunction> context,
            final HolderGetter<DensityFunction> functions,
            final HolderGetter<NormalNoise> noises,
            final ResourceKey<DensityFunction> name,
            final boolean applyEntrances
    ) {
        DensityFunction slopedCheese = DensityFunctions.constant(0.1f); //NoiseRouterData.getFunction(functions, NoiseRouterData.OVERWORLD_FUNCTIONS.slopedCheese());
        DensityFunction surface = slopedCheese;

        if (applyEntrances) {
            surface = DensityFunctions.min(
                    slopedCheese,
                    NoiseRouterData.getFunction(functions, NoiseRouterData.ENTRANCES).mul(5.0f)
            );
        }

        DensityFunction caves = DensityFunctions.rangeChoice(
                slopedCheese,
                -1000000.0f,
                1.5625f,
                surface,
                NoiseRouterData.underground(functions, noises, slopedCheese)
        );

        context.register(
                name,
                DensityFunctions.add(
                        DensityFunctions.min(
                                NoiseRouterData.postProcess(caves, 4, 8),
                                NoiseRouterData.getFunction(functions, NoiseRouterData.NOODLE)
                        ),
                        DensityFunctions.beardifier()
                )
        );
    }

    private DensityFunctionGenerator() {

    }
}