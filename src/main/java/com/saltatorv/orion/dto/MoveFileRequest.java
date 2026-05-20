package com.saltatorv.orion.dto;

import jakarta.validation.constraints.NotBlank;

public record MoveFileRequest(
        @NotBlank String sourcePath,
        @NotBlank String targetDirectory
) {
}