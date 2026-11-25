package com.thedeathlycow.novoatlas.mixin;

import com.mojang.datafixers.DataFixer;
import com.thedeathlycow.novoatlas.impl.NovoAtlas;
import com.thedeathlycow.novoatlas.impl.v2.image.data.ColorImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.data.GrayscaleImageData;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ImageLoader;
import com.thedeathlycow.novoatlas.impl.v2.image.io.NovoAtlasMinecraftServer;
import com.thedeathlycow.novoatlas.impl.v2.image.io.ResourceIndex;
import com.thedeathlycow.novoatlas.impl.v2.registry.NovoAtlasRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.notifications.NotificationManager;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.Optional;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements NovoAtlasMinecraftServer {
    @Unique
    private ImageLoader<GrayscaleImageData> novoatlas_grayscaleLoader;

    @Unique
    private ImageLoader<ColorImageData> novoatlas_colorLoader;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(
            final Thread serverThread,
            final LevelStorageSource.LevelStorageAccess storageSource,
            final PackRepository packRepository,
            final WorldStem worldStem,
            final Optional<GameRules> gameRules,
            final Proxy proxy,
            final DataFixer fixerUpper,
            final Services services,
            final LevelLoadListener levelLoadListener,
            final boolean propagatesCrashes,
            final NotificationManager notificationManager,
            CallbackInfo ci
    ) {
        ResourceManager manager = worldStem.resourceManager();
        this.novoatlas_grayscaleLoader = new ImageLoader<>(NovoAtlasRegistries.GRAYSCALE_IMAGES, manager, GrayscaleImageData::fromBufferedImage);
        this.novoatlas_colorLoader = new ImageLoader<>(NovoAtlasRegistries.COLOR_IMAGES, manager, ColorImageData::fromBufferedImage);
    }

    @Override
    public ImageLoader<GrayscaleImageData> novoatlas$grayscaleLoader() {
        return this.novoatlas_grayscaleLoader;
    }

    @Override
    public ImageLoader<ColorImageData> novoatlas$colorLoader() {
        return this.novoatlas_colorLoader;
    }
}