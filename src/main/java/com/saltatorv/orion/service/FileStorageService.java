package com.saltatorv.orion.service;

import com.saltatorv.orion.config.StorageProperties;
import com.saltatorv.orion.dto.FileItemDto;
import com.saltatorv.orion.dto.PageResponse;
import com.saltatorv.orion.entity.FileMetadata;
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
    private final StoragePathService storagePathService;
    private final FileMetadataService fileMetadataService;
    private final ThumbnailService thumbnailService;

    public void upload(MultipartFile file, String directory) throws IOException {
        Path targetDirectory = resolveDirectory(directory);
        Files.createDirectories(targetDirectory);

        String filename = Path.of(file.getOriginalFilename()).getFileName().toString();
        Path target = targetDirectory.resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        String relativePath = buildRelativePath(directory, filename);
        thumbnailService.generateThumbnail(relativePath);
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

                            FileMetadata metadata = fileMetadataService.getOrCreate(relativePath);

                            return new FileItemDto(
                                    fileName,
                                    relativePath,
                                    Files.size(path),
                                    metadata.isPrinted(),
                                    metadata.isFavorite()
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

    public void renameFile(String path, String newName) throws IOException {
        Path source = getFile(path);

        if (!Files.exists(source) || !Files.isRegularFile(source)) {
            throw new NoSuchFileException("File does not exist: " + path);
        }

        String safeNewName = Path.of(newName).getFileName().toString();

        if (!hasExtension(safeNewName)) {
            safeNewName = safeNewName + getExtension(source.getFileName().toString());
        }
        Path target = source.getParent()
                .resolve(safeNewName)
                .normalize();

        if (!target.startsWith(Paths.get(storageProperties.getPath()).toAbsolutePath().normalize())) {
            throw new SecurityException("Invalid target path");
        }
        if (Files.exists(target)) {
            throw new FileAlreadyExistsException("File already exists: " + safeNewName);
        }

        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
    }

    private String getExtension(String filename) {
        int index = filename.lastIndexOf('.');

        if (index == -1) {
            return "";
        }

        return filename.substring(index);
    }
    public void moveFile(
            String sourcePath,
            String targetDirectory
    ) throws IOException {

        Path source = getFile(sourcePath);

        if (!Files.exists(source) || !Files.isRegularFile(source)) {
            throw new NoSuchFileException("File does not exist: " + sourcePath);
        }

        Path targetDir = storagePathService.resolve(targetDirectory);

        if (!Files.exists(targetDir) || !Files.isDirectory(targetDir)) {
            throw new NoSuchFileException("Directory does not exist: " + targetDirectory);
        }

        Path target = targetDir
                .resolve(source.getFileName())
                .normalize();

        if (!target.startsWith(storagePathService.root())) {
            throw new SecurityException("Invalid target path");
        }

        if (Files.exists(target)) {
            throw new FileAlreadyExistsException(
                    "File already exists in target directory"
            );
        }

        Files.move(source, target);
    }


    private boolean hasExtension(String filename) {
        return filename.lastIndexOf('.') > 0;
    }
}