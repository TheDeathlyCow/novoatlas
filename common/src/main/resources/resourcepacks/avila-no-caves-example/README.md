# Avila No Caves Example Pack

This is an example datapack for NovoAtlas based on the Avila mountains.

This is an example that demonstrates how to fully disable caves within the map. This datapack provides its example in a custom dimension, rather than the Overworld. You can teleport there with this command:

```mcfunction
execute in avila-no-caves-example:avila run teleport @s ~ ~ ~
```

To use this pack, you can simply replace the [height map](data/avila-no-caves-example/novoatlas/heightmap) and [biome maps](data/avila-no-caves-example/novoatlas/biome_map) with your own images. The biome map should be 1/4 the size of the heightmap. For best results, the width and height of images should be some whole-number multiple of 16 (to align with chunk boundaries). However, this is not strictly necessary

This pack is specially licensed under [CC0](./LICENSE), except for the heightmap. You may adapt and modify this pack for your own use with the need for attribution. The heightmap itself is courtesy of Irene Alvarado, https://medium.com/energeia/printing-mountains-6bbf577294b6.