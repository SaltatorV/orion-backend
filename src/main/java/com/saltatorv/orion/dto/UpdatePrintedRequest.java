package com.saltatorv.orion.dto;

public record UpdatePrintedRequest(
        String path,
        boolean printed
) {
}