# NovoAtlas

A data-driven image based world generator for Minecraft. Based on [Atlas](https://www.github.com/itsmiir/atlas/) by itsmiir.

## Installation

These are the official sources of NovoAtlas:

- **GitHub:** https://github.com/TheDeathlyCow/novoatlas/
- **Modrinth:** https://modrinth.com/mod/novoatlas/
- **CurseForge:** https://legacy.curseforge.com/minecraft/mc-mods/novoatlas

## How to Use

The [repository wiki](https://github.com/TheDeathlyCow/novoatlas/wiki) describes how to use this mod to create your own worlds/dimensions.

Example datapacks are builtin with the mod, and can be found in the "Datapacks" folder of the world creation screen. However, they can also be downloaded separately from the [releases page](https://github.com/TheDeathlyCow/novoatlas/releases).

## Build Instructions

NovoAtlas is built using [Gradle](https://gradle.org/) with a modified version of [Jared's Multiloader template](https://github.com/jaredlll08/MultiLoader-Template) to use the [Neo Loom plugin](https://github.com/RelativityMC/neo-loom) instead of Fabric Loom and ModDevGradle. You can build your own copy of NovoAtlas by running the command:

```bash
./gradlew build
# or for a specific loader:
./gradlew :fabric:build
./gradlew :neoforge:build
```

## License

NovoAtlas is licensed under [LGPL-3.0](./LICENSE). The original Atlas mod is licensed under [CC0-1.0](https://github.com/itsmiir/atlas/blob/1.19/LICENSE).
