package com.thedeathlycow.novoatlas.impl.v2.image.io;

import com.google.common.base.Suppliers;
import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ImageData;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.*;
import java.util.function.Supplier;

public final class ImageLoader<T extends ImageData> {
    private final ConcurrentMap<Identifier, CompletableFuture<T>> registry = new ConcurrentHashMap<>();

    private final ResourceKey<Registry<T>> registryId;
    private final Factory<T> factory;
    private final Supplier<ResourceIndex<T>> index;
    private final Executor executor;

    public ImageLoader(ResourceKey<Registry<T>> registryId, ResourceManager manager, Factory<T> factory, Executor executor) {
        this.registryId = registryId;
        this.index = Suppliers.memoize(() -> new ResourceIndex<>(this.registryId, manager));
        this.factory = factory;
        this.executor = executor;
    }

    public ImageLoader(ResourceKey<Registry<T>> registryId, ResourceManager manager, Factory<T> factory) {
        this(registryId, manager, factory, Util.backgroundExecutor());
    }

    @FunctionalInterface
    public interface Factory<T extends @NonNull ImageData> {
        T fromBufferedImage(@NonNull BufferedImage image);
    }

    public CompletableFuture<T> get(Identifier id) {
        CompletableFuture<T> created = new CompletableFuture<>();
        CompletableFuture<T> existing = this.registry.putIfAbsent(id, created);

        if (existing != null) {
            return existing.copy();
        }

        try {
            this.executor.execute(() -> this.run(id, created));
        } catch (RejectedExecutionException e) {
            this.registry.remove(id, created);
            created.completeExceptionally(e);
        }

        return created.copy();
    }

    private void run(Identifier id, CompletableFuture<T> future) {
        try {
            future.complete(this.loadImage(id));
        } catch (Throwable t) {
            NovoAtlas.LOGGER.error("Error loading image [{}]: {}", this.registryId, id, t);
            future.completeExceptionally(t);
        }
    }

    private T loadImage(Identifier cleanId) throws IOException {
        Resource resource = this.index.get().get(cleanId);

        if (resource == null) {
            throw new IOException("Unknown image in registry: [" + this.registryId + "]: " + cleanId);
        }

        BufferedImage image;

        try (InputStream stream = resource.open()) {
            image = ImageIO.read(stream);
        }

        if (image == null) {
            throw new IOException("Unsupported image format in image registry found [" + this.registryId + "]: " + cleanId);
        }

        return this.factory.fromBufferedImage(image);
    }
}