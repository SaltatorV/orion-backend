package com.saltatorv.orion.dto;

public record FileItemDto(
        String name,
        String path,
        long size
) {
}