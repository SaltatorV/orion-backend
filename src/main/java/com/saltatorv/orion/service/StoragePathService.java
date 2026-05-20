package com.saltatorv.orion.service;

import com.saltatorv.orion.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class StoragePathService {

    private final StorageProperties storageProperties;

    public Path root() {
        return Paths.get(storageProperties.getPath())
                .toAbsolutePath()
                .normalize();
    }

    public Path resolve(String relativePath) {
        Path root = root();

        if (relativePath == null || relativePath.isBlank()) {
            return root;
        }

        Path resolved = root.resolve(relativePath).normalize();

        if (!resolved.startsWith(root)) {
            throw new SecurityException("Invalid path");
        }

        return resolved;
    }

    public String toRelativePath(Path path) {
        return root()
                .relativize(path.toAbsolutePath().normalize())
                .toString()
                .replace("\\", "/");
    }


}