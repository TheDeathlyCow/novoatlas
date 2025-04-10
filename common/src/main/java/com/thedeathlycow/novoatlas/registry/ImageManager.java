package com.thedeathlycow.novoatlas.registry;

import com.thedeathlycow.novoatlas.world.gen.MapImage;
import net.minecraft.core.Registry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ImageManager {
    public static final ImageManager HEIGHTMAP = new ImageManager(NovoAtlasResourceKeys.HEIGHTMAP, false);
    public static final ImageManager BIOME_MAP = new ImageManager(NovoAtlasResourceKeys.BIOME_MAP, true);

    private final ResourceKey<Registry<MapImage>> registryKey;
    private final boolean color;
    private final Map<ResourceKey<MapImage>, MapImage> registry = new IdentityHashMap<>();

    private ImageManager(ResourceKey<Registry<MapImage>> registryKey, boolean color) {
        this.registryKey = registryKey;
        this.color = color;
    }

    public void reload(ResourceManager resourceManager) {
        Map<ResourceKey<MapImage>, MapImage> updatedRegistry = new IdentityHashMap<>();

        String regPath = "novoatlas/" + registryKey.location().getPath();
        var converter = new FileToIdConverter(regPath, ".png");
        Map<ResourceLocation, Resource> resources = converter.listMatchingResources(resourceManager);

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            BufferedImage image;
            try (InputStream stream = entry.getValue().open()) {
                image = ImageIO.read(stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            MapImage map = MapImage.fromBufferedImage(image, color);
            ResourceKey<MapImage> key = ResourceKey.create(registryKey, converter.fileToId(entry.getKey()));

            updatedRegistry.put(key, map);
        }

        this.registry.clear();
        this.registry.putAll(updatedRegistry);
    }

    @Nullable
    public MapImage getImage(ResourceKey<MapImage> key) {
        return this.registry.get(key);
    }
}