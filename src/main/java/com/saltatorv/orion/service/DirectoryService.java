package com.saltatorv.orion.service;

import com.saltatorv.orion.dto.DirectoryItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectoryService {

    private final StoragePathService storagePathService;

    public List<DirectoryItemDto> listDirectories(String path) throws IOException {
        Path directory = storagePathService.resolve(path);

        if (!Files.exists(directory)) {
            return List.of();
        }

        try (var stream = Files.list(directory)) {
            return stream
                    .filter(Files::isDirectory)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .map(p -> new DirectoryItemDto(
                            p.getFileName().toString(),
                            storagePathService.toRelativePath(p)
                    ))
                    .toList();
        }
    }

    public void createDirectory(String path) throws IOException {
        Path directory = storagePathService.resolve(path);
        Files.createDirectories(directory);
    }

    public void renameDirectory(String path, String newName) throws IOException {
        Path source = storagePathService.resolve(path);

        if (!Files.exists(source) || !Files.isDirectory(source)) {
            throw new NoSuchFileException("Directory does not exist: " + path);
        }

        String safeNewName = Path.of(newName).getFileName().toString();

        Path target = source.getParent().resolve(safeNewName).normalize();

        if (!target.startsWith(storagePathService.root())) {
            throw new SecurityException("Invalid target path");
        }

        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
    }

    public void deleteDirectory(String path) throws IOException {
        Path directory = storagePathService.resolve(path);

        if (!Files.exists(directory)) {
            return;
        }

        if (directory.equals(storagePathService.root())) {
            throw new SecurityException("Cannot delete storage root");
        }

        try (var walk = Files.walk(directory)) {
            List<Path> paths = walk
                    .sorted(Comparator.reverseOrder())
                    .toList();

            for (Path currentPath : paths) {
                Files.deleteIfExists(currentPath);
            }
        }
    }
}