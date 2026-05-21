package com.saltatorv.orion.service;

import com.saltatorv.orion.entity.FileMetadata;
import com.saltatorv.orion.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private final FileMetadataRepository repository;

    public FileMetadata getOrCreate(String path) {
        return repository.findByPath(path)
                .orElseGet(() -> {
                    FileMetadata metadata = new FileMetadata();
                    metadata.setPath(path);
                    return repository.save(metadata);
                });
    }

    public void delete(String path) {
        repository.deleteByPath(path);
    }

    public FileMetadata updatePrinted(String path, boolean printed) {
        FileMetadata metadata = getOrCreate(path);
        metadata.setPrinted(printed);
        return repository.save(metadata);
    }

    @Transactional
    public void movePath(String oldPath, String newPath) {

        repository.findByPath(oldPath)
                .ifPresent(metadata -> {
                    metadata.setPath(newPath);
                    repository.save(metadata);
                });
    }
}