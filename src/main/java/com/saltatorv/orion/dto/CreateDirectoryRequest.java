package com.saltatorv.orion.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDirectoryRequest(
        @NotBlank String name
) {
}