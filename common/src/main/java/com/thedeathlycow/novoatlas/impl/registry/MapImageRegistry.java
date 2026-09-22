package com.thedeathlycow.novoatlas.impl.registry;

import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.image.BiomeMapImage;
import com.thedeathlycow.novoatlas.impl.image.HeightMapImage;
import com.thedeathlycow.novoatlas.impl.image.MapImage;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

public class MapImageRegistry<T extends MapImage> extends SimplePreparableReloadListener<Map<ResourceKey<T>, T>> {
    public static final MapImageRegistry<HeightMapImage> HEIGHTMAP = new MapImageRegistry<>(
            NovoAtlasRegistries.HEIGHTMAP,
            HeightMapImage::fromBufferedImage
    );
    public static final MapImageRegistry<BiomeMapImage> BIOME_MAP = new MapImageRegistry<>(
            NovoAtlasRegistries.BIOME_MAP,
            BiomeMapImage::fromBufferedImage
    );

    private final ResourceKey<Registry<T>> registryKey;
    private final Map<ResourceKey<T>, T> registry;
    private final Factory<T> factory;

    private MapImageRegistry(ResourceKey<Registry<T>> registryKey, Factory<T> factory) {
        this.registryKey = registryKey;
        this.registry = new IdentityHashMap<>();
        this.factory = factory;
    }

    public ResourceKey<Registry<T>> registryKey() {
        return registryKey;
    }

    @Nullable
    public T getImage(ResourceKey<T> key) {
        return this.registry.get(key);
    }

    @Override
    @NotNull
    public Map<ResourceKey<T>, T> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceKey<T>, T> updatedRegistry = new IdentityHashMap<>();

        var converter = MultiFileTypeToIdConverter.imageRegistry(registryKey);
        if (NovoAtlas.LOGGER.isInfoEnabled()) {
            NovoAtlas.LOGGER.info(
                    "Reloading map images for {}, supported image formats are: {}",
                    registryKey.location(),
                    Arrays.toString(converter.getExtensions())
            );
        }

        Map<ResourceLocation, Resource> resources = converter.listMatchingResources(manager);

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            BufferedImage image;
            try (InputStream stream = entry.getValue().open()) {
                image = ImageIO.read(stream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            T map = this.factory.fromBufferedImage(image);
            ResourceKey<T> key = ResourceKey.create(registryKey, converter.fileToId(entry.getKey()));

            if (updatedRegistry.put(key, map) != null) {
                final String message = "Found duplicate image files for {}, overriding with {} " +
                        "(images of the different types should have different names)";
                NovoAtlas.LOGGER.warn(message, key.location(), entry.getKey());
            }
        }

        return updatedRegistry;
    }

    @Override
    public void apply(Map<ResourceKey<T>, T> preparations, ResourceManager manager, ProfilerFiller profiler) {
        this.registry.clear();
        this.registry.putAll(preparations);
        NovoAtlas.LOGGER.info("Loaded {} map image(s) for {}", this.registry.size(), registryKey.location());
    }

    private interface Factory<T extends MapImage> {
        T fromBufferedImage(BufferedImage image);
    }
}