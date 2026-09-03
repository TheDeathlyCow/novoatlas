package com.thedeathlycow.novoatlas.impl.registry;

import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.image.MapImage;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ImageManager extends SimplePreparableReloadListener<Map<ResourceKey<MapImage>, MapImage> > {
    public static final ImageManager HEIGHTMAP = new ImageManager(NovoAtlasRegistries.HEIGHTMAP, MapImage.Type.HEIGHTMAP);
    public static final ImageManager BIOME_MAP = new ImageManager(NovoAtlasRegistries.BIOME_MAP, MapImage.Type.BIOME_MAP);

    private final ResourceKey<Registry<MapImage>> registryKey;
    private final MapImage.Type type;
    private final Map<ResourceKey<MapImage>, MapImage> registry = new IdentityHashMap<>();

    private ImageManager(ResourceKey<Registry<MapImage>> registryKey, MapImage.Type type) {
        this.registryKey = registryKey;
        this.type = type;
    }

    @Nullable
    public MapImage getImage(ResourceKey<MapImage> key) {
        return this.registry.get(key);
    }

    @Override
    protected Map<ResourceKey<MapImage>, MapImage> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceKey<MapImage>, MapImage> updatedRegistry = new IdentityHashMap<>();

        var converter = MultiFileTypeToIdConverter.imageRegistry(registryKey);
        if (NovoAtlas.LOGGER.isInfoEnabled()) {
            NovoAtlas.LOGGER.info(
                    "Reloading map images for {}, supported image formats are: {}",
                    registryKey.identifier(),
                    Arrays.toString(converter.getExtensions())
            );
        }

        Map<Identifier, Resource> resources = converter.listMatchingResources(manager);

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            BufferedImage image;
            try (InputStream stream = entry.getValue().open()) {
                image = ImageIO.read(stream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            MapImage map = MapImage.fromBufferedImage(image, this.type);
            ResourceKey<MapImage> key = ResourceKey.create(registryKey, converter.fileToId(entry.getKey()));

            if (updatedRegistry.put(key, map) != null) {
                final String message = "Found duplicate image files for {}, overriding with {} " +
                        "(images of the different types should have different names)";
                NovoAtlas.LOGGER.warn(message, key.identifier(), entry.getKey());
            }
        }

        return updatedRegistry;
    }

    @Override
    protected void apply(Map<ResourceKey<MapImage>, MapImage> preparations, ResourceManager manager, ProfilerFiller profiler) {
        this.registry.clear();
        this.registry.putAll(preparations);
        NovoAtlas.LOGGER.info("Loaded {} map image(s) for {}", this.registry.size(), registryKey.identifier());
    }
}