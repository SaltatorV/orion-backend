package com.saltatorv.orion.controller;

import com.saltatorv.orion.dto.CreateDirectoryRequest;
import com.saltatorv.orion.dto.DirectoryItemDto;
import com.saltatorv.orion.dto.RenameDirectoryRequest;
import com.saltatorv.orion.service.DirectoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/directories")
@RequiredArgsConstructor
public class DirectoryController {

    private final DirectoryService directoryService;

    @GetMapping
    public ResponseEntity<List<DirectoryItemDto>> listDirectories(
            @RequestParam(value = "path", required = false) String path
    ) throws IOException {
        return ResponseEntity.ok(directoryService.listDirectories(path));
    }

    @PostMapping
    public ResponseEntity<Void> createDirectory(
            @Valid @RequestBody CreateDirectoryRequest request
    ) throws IOException {
        directoryService.createDirectory(request.path());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/rename")
    public ResponseEntity<Void> renameDirectory(
            @Valid @RequestBody RenameDirectoryRequest request
    ) throws IOException {
        directoryService.renameDirectory(request.path(), request.newName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteDirectory(
            @RequestParam String path
    ) throws IOException {
        directoryService.deleteDirectory(path);
        return ResponseEntity.noContent().build();
    }
}