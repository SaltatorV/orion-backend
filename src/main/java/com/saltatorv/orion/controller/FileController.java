package com.saltatorv.orion.controller;

import com.saltatorv.orion.service.FileStorageService;
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
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        fileStorageService.upload(file);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<String>> getFiles() throws IOException {

        return ResponseEntity.ok(
                fileStorageService.getFiles()
        );
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String filename
    ) throws IOException {

        Path file = fileStorageService.getFile(filename);

        Resource resource = new UrlResource(file.toUri());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + filename + "\""
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<Void> delete(
            @PathVariable String filename
    ) throws IOException {

        fileStorageService.delete(filename);

        return ResponseEntity.noContent().build();
    }
}