package com.saltatorv.orion.service;

import com.saltatorv.orion.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final StorageProperties storageProperties;

    public void upload(MultipartFile file) throws IOException {

        Path storagePath = Paths.get(storageProperties.getPath());

        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        Path target = storagePath.resolve(file.getOriginalFilename());

        Files.copy(
                file.getInputStream(),
                target,
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    public List<String> getFiles() throws IOException {

        Path storagePath = Paths.get(storageProperties.getPath());

        if (!Files.exists(storagePath)) {
            return List.of();
        }

        try (var stream = Files.list(storagePath)) {

            return stream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .toList();
        }
    }

    public Path getFile(String filename) {

        return Paths
                .get(storageProperties.getPath())
                .resolve(filename);
    }

    public void delete(String filename) throws IOException {

        Files.deleteIfExists(getFile(filename));
    }
}