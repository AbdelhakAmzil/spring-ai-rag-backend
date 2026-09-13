package com.abdelhak.ragdemo.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatMessageDto(UUID id, String role, String content, Instant createdAt, List<String> sources) {
}