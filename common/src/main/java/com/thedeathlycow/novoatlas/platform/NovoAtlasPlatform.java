package com.thedeathlycow.novoatlas.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface NovoAtlasPlatform {
    /// Gets the name of the current platform
    ///
    /// @return The name of the current platform.
    String getPlatformName();

    /// Checks if a mod with the given id is loaded.
    ///
    /// @param modid The mod to check if it is loaded.
    /// @return `true` if the mod is loaded, `false` otherwise.
    boolean isModLoaded(String modid);

    ///  Checks if a mod with the given id is loaded, for early loading contexts.
    ///
    /// @param modid The mod to check if it is loaded.
    /// @return `true` if the mod is loaded, `false` otherwise.
    default boolean isEarlyModLoaded(String modid) {
        return isModLoaded(modid);
    }

    /// Check if the game is currently in a development environment.
    ///
    /// @return `true` if in a development environment, `false` otherwise.
    boolean isDevelopmentEnvironment();

    /// Gets the name of the environment type as a string.
    ///
    /// @return The name of the environment type.
    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }

    /// Creates a server-side friendly built in registry from a key.
    <T> Registry<T> createBuiltinRegistry(ResourceKey<Registry<T>> key);
}