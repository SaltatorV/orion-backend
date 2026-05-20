package com.saltatorv.orion.repository;

import com.saltatorv.orion.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByPath(String path);

    boolean existsByPath(String path);

    void deleteByPath(String path);
}