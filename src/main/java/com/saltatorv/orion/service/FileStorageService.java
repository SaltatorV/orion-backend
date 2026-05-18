package com.saltatorv.orion.service;

import com.saltatorv.orion.config.StorageProperties;
import com.saltatorv.orion.dto.FileItemDto;
import com.saltatorv.orion.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final StorageProperties storageProperties;

    public void upload(MultipartFile file, String directory) throws IOException {
        Path targetDirectory = resolveDirectory(directory);
        Files.createDirectories(targetDirectory);

        String filename = Path.of(file.getOriginalFilename()).getFileName().toString();
        Path target = targetDirectory.resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
    }

    public PageResponse<FileItemDto> getFiles(String directory, int page, int size) throws IOException {
        Path targetDirectory = resolveDirectory(directory);

        if (!Files.exists(targetDirectory)) {
            return new PageResponse<>(List.of(), page, size, 0, 0);
        }

        List<FileItemDto> allFiles;

        try (var stream = Files.list(targetDirectory)) {
            allFiles = stream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(path -> {
                        try {
                            String fileName = path.getFileName().toString();
                            String relativePath = buildRelativePath(directory, fileName);

                            return new FileItemDto(
                                    fileName,
                                    relativePath,
                                    Files.size(path)
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        }

        int fromIndex = Math.min(page * size, allFiles.size());
        int toIndex = Math.min(fromIndex + size, allFiles.size());

        List<FileItemDto> content = allFiles.subList(fromIndex, toIndex);

        int totalPages = (int) Math.ceil((double) allFiles.size() / size);

        return new PageResponse<>(
                content,
                page,
                size,
                allFiles.size(),
                totalPages
        );
    }

    public List<String> getDirectories() throws IOException {
        Path root = getRootStoragePath();

        if (!Files.exists(root)) {
            return List.of();
        }

        try (var stream = Files.list(root)) {
            return stream
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
        }
    }

    public void createDirectory(String name) throws IOException {
        Path directory = resolveDirectory(name);
        Files.createDirectories(directory);
    }

    public Path getFile(String relativePath) {
        return getRootStoragePath()
                .resolve(relativePath)
                .normalize();
    }

    public void delete(String relativePath) throws IOException {
        Path file = getFile(relativePath);

        if (!file.startsWith(getRootStoragePath())) {
            throw new SecurityException("Invalid file path");
        }

        Files.deleteIfExists(file);
    }

    private Path resolveDirectory(String directory) {
        Path root = getRootStoragePath();

        if (directory == null || directory.isBlank()) {
            return root;
        }

        Path resolved = root.resolve(directory).normalize();

        if (!resolved.startsWith(root)) {
            throw new SecurityException("Invalid directory path");
        }

        return resolved;
    }

    private Path getRootStoragePath() {
        return Paths.get(storageProperties.getPath())
                .toAbsolutePath()
                .normalize();
    }

    private String buildRelativePath(String directory, String fileName) {
        if (directory == null || directory.isBlank()) {
            return fileName;
        }

        return directory + "/" + fileName;
    }
}