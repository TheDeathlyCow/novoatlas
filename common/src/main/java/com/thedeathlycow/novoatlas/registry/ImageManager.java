package com.thedeathlycow.novoatlas.registry;

import com.thedeathlycow.novoatlas.NovoAtlas;
import com.thedeathlycow.novoatlas.util.TilingHelper;
import com.thedeathlycow.novoatlas.world.gen.MapImage;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImageManager {
    public static final ImageManager HEIGHTMAP = new ImageManager(NovoAtlasResourceKeys.HEIGHTMAP, MapImage.Type.HEIGHTMAP);
    public static final ImageManager BIOME_MAP = new ImageManager(NovoAtlasResourceKeys.BIOME_MAP, MapImage.Type.BIOME_MAP);
    private static final String[] SUPPORTED_SUFFIXES = ImageIO.getReaderFileSuffixes();

    private final ResourceKey<Registry<MapImage>> registryKey;
    private final MapImage.Type type;
    private final Path cacheRoot;
    private final Map<ResourceKey<MapImage>, MapImage> registry = new IdentityHashMap<>();

    private ImageManager(ResourceKey<Registry<MapImage>> registryKey, MapImage.Type type) {
        this.registryKey = registryKey;
        this.type = type;
        this.cacheRoot = NovoAtlas.getCacheDirectory();
    }

    public void reload(ResourceManager resourceManager) {
        this.registry.clear();
        String regPath = NovoAtlas.MOD_ID + "/" + registryKey.location().getPath();

        Map<ResourceLocation, Resource> imageResources = resourceManager.listResources(regPath, location -> {
            String path = location.getPath();
            for (String suffix : SUPPORTED_SUFFIXES) {
                if (path.endsWith("." + suffix)) {
                    return true;
                }
            }
            return false;
        });

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);
        int tasks = 0;

        for (Map.Entry<ResourceLocation, Resource> entry : imageResources.entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            Resource resource = entry.getValue();

            completionService.submit(() -> {
                try (InputStream stream = resource.open()) {
                    BufferedImage image = ImageIO.read(stream);
                    if (image == null) {
                        NovoAtlas.LOGGER.warn("Could not read image file: {}, it might be an unsupported format or corrupted.", resourceLocation);
                        return null;
                    }

                    String fullPath = resourceLocation.getPath();
                    String prefix = regPath + "/";
                    String relativePath = fullPath.substring(prefix.length());
                    String idPath = relativePath.substring(0, relativePath.lastIndexOf('.'));
                    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), idPath);

                    ResourceKey<MapImage> key = ResourceKey.create(registryKey, id);

                    Path imageBaseCacheDir = this.cacheRoot
                            .resolve(resourceLocation.getNamespace())
                            .resolve(idPath);

                    TilingHelper.tileImage(image, imageBaseCacheDir, this.type);

                    MapImage map = new MapImage(imageBaseCacheDir, this.type);
                    this.registry.put(key, map);
                } catch (IOException | NoSuchAlgorithmException e) {
                    NovoAtlas.LOGGER.error("Failed to process image: {}", resourceLocation, e);
                }
                return null;
            });
            tasks++;
        }

        for (int i = 0; i < tasks; i++) {
            try {
                completionService.take().get();
            } catch (Exception e) {
                NovoAtlas.LOGGER.error("Error during image tiling: {}", e.getMessage());
            }
        }
        executor.shutdown();

        NovoAtlas.LOGGER.info("Reloaded map images for {}", registryKey);
    }

    @Nullable
    public MapImage getImage(ResourceKey<MapImage> key) {
        return this.registry.get(key);
    }
}
