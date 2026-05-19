package com.saltatorv.orion.dto;

import jakarta.validation.constraints.NotBlank;

public record RenameDirectoryRequest(
        @NotBlank String path,
        @NotBlank String newName
) {
}