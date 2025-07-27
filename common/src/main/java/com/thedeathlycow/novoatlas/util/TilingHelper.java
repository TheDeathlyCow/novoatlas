package com.thedeathlycow.novoatlas.util;

import com.thedeathlycow.novoatlas.world.gen.MapImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class TilingHelper {

    private static final int TILE_SIZE = 16;
    private static final String HASH_ALGORITHM = "SHA-512";

    public static void tileImage(BufferedImage image, Path baseCacheDir, MapImage.Type type) throws IOException, NoSuchAlgorithmException {
        String hash = generateSha512(image);
        Path hashFilePath = baseCacheDir.resolve("imageHash");

        if (Files.exists(hashFilePath)) {
            try (BufferedReader reader = Files.newBufferedReader(hashFilePath)) {
                String cachedHash = reader.readLine();
                if (hash.equals(cachedHash)) {
                    return;
                }
            }
        }

        // Clear old tiles
        DirectoryUtil.deleteDirectory(baseCacheDir);
        DirectoryUtil.createDirectories(baseCacheDir);

        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y += TILE_SIZE) {
            for (int x = 0; x < width; x += TILE_SIZE) {
                int tileX = x / TILE_SIZE;
                int tileY = y / TILE_SIZE;

                int tileWidth = Math.min(TILE_SIZE, width - x);
                int tileHeight = Math.min(TILE_SIZE, height - y);

                BufferedImage tile = image.getSubimage(x, y, tileWidth, tileHeight);

                Path tileDir = baseCacheDir.resolve(String.valueOf(tileX)).resolve(String.valueOf(tileY));
                Files.createDirectories(tileDir);
                Path tilePath = tileDir.resolve(type.name().toLowerCase() + ".png");

                ImageIO.write(tile, "png", tilePath.toFile());
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(hashFilePath)) {
            writer.write(hash);
        }
    }

    private static String generateSha512(BufferedImage image) throws NoSuchAlgorithmException, IOException {
        MessageDigest sha512 = MessageDigest.getInstance(HASH_ALGORITHM);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean success = ImageIO.write(image, "png", outputStream);

        if (!success) {
            throw new IOException("Could not convert image to byte array for hashing.");
        }

        byte[] imageBytes = outputStream.toByteArray();
        byte[] digest = sha512.digest(imageBytes);
        return Base64.getEncoder().encodeToString(digest);
    }
}
