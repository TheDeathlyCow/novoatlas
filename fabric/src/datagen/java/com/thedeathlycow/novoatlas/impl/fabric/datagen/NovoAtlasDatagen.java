package com.thedeathlycow.novoatlas.impl.fabric.datagen;

import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.fabric.datagen.bootstrap.DensityFunctionGenerator;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import org.jspecify.annotations.Nullable;

public class NovoAtlasDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(BootstrappedRegistryProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder builder) {
        DataGeneratorEntrypoint.super.buildRegistry(builder);
        builder.add(
                Registries.DENSITY_FUNCTION,
                DensityFunctionGenerator::bootstrap
        );
    }

    @Override
    public @Nullable String getEffectiveModId() {
        return NovoAtlas.MOD_ID;
    }
}