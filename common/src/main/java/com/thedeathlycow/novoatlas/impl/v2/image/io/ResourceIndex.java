package com.thedeathlycow.novoatlas.impl.v2.image.io;

import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ColorImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.data.GrayscaleImageData;
import com.thedeathlycow.novoatlas.impl.v2.registry.NovoAtlasRegistries;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/// Keeps a map of IDs to resources at the top of each resource stack that is refreshed upon reload.
public final class ResourceIndex<T> extends SimplePreparableReloadListener<Map<Identifier, Resource>> {
    public static final ResourceIndex<GrayscaleImageData> GRAYSCALE_IMAGES = new ResourceIndex<>(NovoAtlasRegistries.GRAYSCALE_IMAGES);
    public static final ResourceIndex<ColorImageData> COLOR_IMAGES = new ResourceIndex<>(NovoAtlasRegistries.COLOR_IMAGES);

    private final Object2ObjectMap<Identifier, Resource> index = new Object2ObjectOpenHashMap<>();
    private final ResourceKey<Registry<T>> registryId;
    private final MultiFileTypeToIdConverter converter;
    private boolean bound;

    public ResourceIndex(ResourceKey<Registry<T>> registryId, MultiFileTypeToIdConverter converter) {
        this.registryId = registryId;
        this.converter = converter;
        this.bound = false;
    }

    public ResourceIndex(ResourceKey<Registry<T>> registryId) {
        this(registryId, MultiFileTypeToIdConverter.registry(registryId));
    }

    @Override
    protected Map<Identifier, Resource> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, Resource> preparations = new Object2ObjectOpenHashMap<>();

        for (Map.Entry<Identifier, Resource> entry : this.converter.listMatchingResources(manager).entrySet()) {
            Identifier key = this.converter.fileToId(entry.getKey());

            if (preparations.put(key, entry.getValue()) != null) {
                final String message = "Found duplicate image files for {}, overriding with {} " +
                        "(images of the different types should have different names)";
                NovoAtlas.LOGGER.warn(message, key, entry.getKey());
            }
        }

        return preparations;
    }

    @Override
    protected void apply(Map<Identifier, Resource> preparations, ResourceManager manager, ProfilerFiller profiler) {
        this.index.clear();
        this.index.putAll(preparations);
        this.bound = true;
    }

    @Nullable
    public Resource get(@NonNull Identifier id) {
        if (!this.bound) {
            throw new IllegalStateException("Resource index " + this.registryId + " not yet bound!");
        }

        return this.index.get(id);
    }
}