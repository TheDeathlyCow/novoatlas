package com.thedeathlycow.novoatlas.impl.fabric;

import com.thedeathlycow.novoatlas.impl.image.MapImage;
import com.thedeathlycow.novoatlas.impl.registry.MapImageRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public record ImageRegistryWrapper<T extends MapImage>(
        MapImageRegistry<T> registry
) implements IdentifiableResourceReloadListener {

    @Override
    public ResourceLocation getFabricId() {
        return this.registry.registryKey().location();
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        CompletableFuture<Map<ResourceKey<T>, T>> preparations = CompletableFuture.supplyAsync(
                () -> this.registry.prepare(resourceManager, profilerFiller),
                executor
        );
        Objects.requireNonNull(preparationBarrier);
        return preparations.thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(
                        (object) -> this.registry.apply(object, resourceManager, profilerFiller2),
                        executor2
                );
    }
}