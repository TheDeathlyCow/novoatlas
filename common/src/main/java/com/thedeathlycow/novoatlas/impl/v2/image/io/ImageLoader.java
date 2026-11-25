package com.thedeathlycow.novoatlas.impl.v2.image.io;

import com.thedeathlycow.novoatlas.impl.v2.image.data.ImageData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.*;

public final class ImageLoader<T extends ImageData> implements AutoCloseable {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final ConcurrentMap<Identifier, CompletableFuture<T>> registry = new ConcurrentHashMap<>();

    private final Identifier registryId;
    private final ResourceManager manager;
    private final Factory<T> factory;

    public ImageLoader(Identifier registryId, ResourceManager manager, Factory<T> factory) {
        this.registryId = registryId;
        this.manager = manager;
        this.factory = factory;
    }

    public CompletableFuture<T> get(Identifier id) {
        return this.registry.computeIfAbsent(id, i -> CompletableFuture.supplyAsync(() -> {
            try {
                return this.loadImage(i);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, this.executor));
    }

    private T loadImage(Identifier id) throws IOException {
        var converter = MultiFileTypeToIdConverter.imageRegistry(this.registryId);

        Resource resource = converter.getResource(id, manager).orElse(null);

        if (resource == null) {
            throw new IOException("Unknown image in registry: [" + this.registryId + "]: " + id);
        }

        BufferedImage image;
        try (InputStream stream = resource.open()) {
            image = ImageIO.read(stream);
        }

        return this.factory.fromBufferedImage(image);
    }

    @Override
    public void close() {
        this.executor.close();
    }

    @FunctionalInterface
    public interface Factory<T extends ImageData> {
        T fromBufferedImage(BufferedImage image);
    }
}