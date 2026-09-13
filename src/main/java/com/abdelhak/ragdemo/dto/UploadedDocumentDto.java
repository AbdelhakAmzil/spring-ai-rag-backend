package com.abdelhak.ragdemo.dto;

import java.time.Instant;

public record UploadedDocumentDto(String fileName, int chunksIndexed, Instant uploadedAt) {
}