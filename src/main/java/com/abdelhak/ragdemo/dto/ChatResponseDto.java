package com.abdelhak.ragdemo.dto;

import java.util.List;
import java.util.UUID;

public record ChatResponseDto(
        String answer,
        List<String> sources,
        UUID conversationId,
        long responseTimeMs,
        int retrievedChunks,
        String intent,
        String model
) {
}