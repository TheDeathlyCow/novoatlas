package com.thedeathlycow.novoatlas.impl.registry;

import com.thedeathlycow.novoatlas.impl.image.MapImage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.imageio.ImageIO;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MultiFileTypeToIdConverter {
    private final String prefix;
    private final String[] extensions;

    public MultiFileTypeToIdConverter(String prefix, String[] extensions) {
        this.prefix = prefix;
        this.extensions = extensions;
    }

    public static MultiFileTypeToIdConverter builtinImages(String prefix) {
        String[] extensions = Arrays.stream(ImageIO.getReaderFileSuffixes())
                .map(suffix -> "." + suffix)
                .toArray(String[]::new);

        return new MultiFileTypeToIdConverter(prefix, extensions);
    }

    public static <T extends MapImage> MultiFileTypeToIdConverter imageRegistry(ResourceKey<Registry<T>> registryKey) {
        return builtinImages(getElementsPath(registryKey));
    }

    public Identifier fileToId(Identifier file) {
        String path = file.getPath();

        for (String extension : this.extensions) {
            if (path.endsWith(extension)) {
                return file.withPath(path.substring(this.prefix.length() + 1, path.length() - extension.length()));
            }
        }

        String message = "Unknown image format '%s', supported formats are: %s"
                .formatted(path, Arrays.toString(this.extensions));

        throw new IllegalArgumentException(message);
    }

    public Map<Identifier, Resource> listMatchingResources(ResourceManager resourceManager) {
        return resourceManager.listResources(this.prefix, this::test);
    }

    public Map<Identifier, List<Resource>> listMatchingResourceStacks(ResourceManager resourceManager) {
        return resourceManager.listResourceStacks(this.prefix, this::test);
    }

    public String[] getExtensions() {
        return this.extensions;
    }

    private boolean test(Identifier location) {
        return Arrays.stream(this.extensions).anyMatch(extension -> location.getPath().endsWith(extension));
    }

    private static String getElementsPath(ResourceKey<? extends Registry<?>> registryKey) {
        String namespace = registryKey.identifier().getNamespace();

        if (namespace.equals(Identifier.DEFAULT_NAMESPACE)) {
            return Registries.elementsDirPath(registryKey);
        } else {
            return "%s/%s".formatted(namespace, registryKey.identifier().getPath());
        }
    }
}