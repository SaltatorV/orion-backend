package com.saltatorv.orion.service;

import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class ThumbnailService {

    private final StoragePathService storagePathService;

    public void generateThumbnail(String relativePath) throws IOException {
        Path original = storagePathService.resolve(relativePath);

        if (!Files.exists(original) || !Files.isRegularFile(original)) {
            return;
        }

        if (!isImage(original)) {
            return;
        }

        Path thumbnail = getThumbnailPath(relativePath);
        Files.createDirectories(thumbnail.getParent());

        Thumbnails.of(original.toFile())
                .size(360, 360)
                .outputQuality(0.82)
                .toFile(thumbnail.toFile());
    }

    public Path getThumbnailPath(String relativePath) {
        return storagePathService.root()
                .resolve(".thumbnails")
                .resolve(relativePath)
                .normalize();
    }

    public boolean thumbnailExists(String relativePath) {
        return Files.exists(getThumbnailPath(relativePath));
    }

    private boolean isImage(Path path) {
        String filename = path.getFileName().toString().toLowerCase();

        return filename.endsWith(".jpg")
                || filename.endsWith(".jpeg")
                || filename.endsWith(".png")
                || filename.endsWith(".webp");
    }
}