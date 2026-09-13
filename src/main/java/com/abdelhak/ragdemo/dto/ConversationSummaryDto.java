package com.abdelhak.ragdemo.dto;

import java.time.Instant;
import java.util.UUID;

public record ConversationSummaryDto(UUID id, String title, Instant createdAt) {
}