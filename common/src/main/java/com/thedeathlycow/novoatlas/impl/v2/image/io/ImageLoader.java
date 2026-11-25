package com.thedeathlycow.novoatlas.impl.v2.image.io;

import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ImageData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jspecify.annotations.NonNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.*;

public final class ImageLoader<T extends ImageData> implements AutoCloseable {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final ConcurrentMap<Identifier, CompletableFuture<T>> registry = new ConcurrentHashMap<>();

    private final Identifier registryId;
    private final Factory<T> factory;
    private final ResourceIndex<T> index;

    public ImageLoader(Identifier registryId, ResourceIndex<T> index, Factory<T> factory) {
        this.registryId = registryId;
        this.index = index;
        this.factory = factory;
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
        Resource resource = this.index.get(cleanId);

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

    @Override
    public void close() {
        this.executor.close();
    }
}