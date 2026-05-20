package com.saltatorv.orion.service;

import com.saltatorv.orion.entity.FileMetadata;
import com.saltatorv.orion.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}