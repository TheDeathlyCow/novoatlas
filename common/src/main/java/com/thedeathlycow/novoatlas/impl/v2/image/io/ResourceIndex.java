package com.thedeathlycow.novoatlas.impl.v2.image.io;

import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/// Keeps a map of IDs to resources at the top of each resource stack that is refreshed upon reload.
public final class ResourceIndex<T> {
    private final Object2ObjectMap<Identifier, Resource> index;
    private final ResourceKey<Registry<T>> registryId;
    private final MultiFileTypeToIdConverter converter;

    public ResourceIndex(ResourceKey<Registry<T>> registryId, ResourceManager manager, MultiFileTypeToIdConverter converter) {
        this.registryId = registryId;
        this.converter = converter;
        this.index = prepare(manager, converter);
    }

    public ResourceIndex(ResourceKey<Registry<T>> registryId, ResourceManager manager) {
        this(registryId, manager, MultiFileTypeToIdConverter.registry(registryId));
    }

    private static Object2ObjectMap<Identifier, Resource> prepare(ResourceManager manager, MultiFileTypeToIdConverter converter) {
        Object2ObjectMap<Identifier, Resource> preparations = new Object2ObjectOpenHashMap<>();

        for (Map.Entry<Identifier, Resource> entry : converter.listMatchingResources(manager).entrySet()) {
            Identifier key = converter.fileToId(entry.getKey());

            if (preparations.put(key, entry.getValue()) != null) {
                final String message = "Found duplicate image files for {}, overriding with {} " +
                        "(images of the different types should have different names)";
                NovoAtlas.LOGGER.warn(message, key, entry.getKey());
            }
        }

        return preparations;
    }

    @Nullable
    public Resource get(@NonNull Identifier id) {
        return this.index.get(id);
    }
}