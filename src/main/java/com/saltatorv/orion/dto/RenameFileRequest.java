package com.saltatorv.orion.dto;

import jakarta.validation.constraints.NotBlank;

public record RenameFileRequest(
        @NotBlank String path,
        @NotBlank String newName
) {
}