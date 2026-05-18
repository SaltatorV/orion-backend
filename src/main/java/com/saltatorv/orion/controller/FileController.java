package com.saltatorv.orion.controller;

import com.saltatorv.orion.dto.CreateDirectoryRequest;
import com.saltatorv.orion.dto.FileItemDto;
import com.saltatorv.orion.dto.PageResponse;
import com.saltatorv.orion.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<Void> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directory", required = false) String directory
    ) throws IOException {
        fileStorageService.upload(file, directory);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<PageResponse<FileItemDto>> getFiles(
            @RequestParam(value = "directory", required = false) String directory,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) throws IOException {
        return ResponseEntity.ok(
                fileStorageService.getFiles(directory, page, size)
        );
    }

    @GetMapping("/directories")
    public ResponseEntity<List<String>> getDirectories() throws IOException {
        return ResponseEntity.ok(fileStorageService.getDirectories());
    }

    @PostMapping("/directories")
    public ResponseEntity<Void> createDirectory(
            @Valid @RequestBody CreateDirectoryRequest request
    ) throws IOException {
        fileStorageService.createDirectory(request.name());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> getFile(
            @RequestParam String path
    ) throws IOException {
        Path file = fileStorageService.getFile(path);
        Resource resource = new UrlResource(file.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @RequestParam String path
    ) throws IOException {
        fileStorageService.delete(path);
        return ResponseEntity.noContent().build();
    }
}