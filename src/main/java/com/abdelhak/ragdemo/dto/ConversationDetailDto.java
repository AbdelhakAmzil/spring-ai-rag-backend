package com.abdelhak.ragdemo.dto;

import java.util.List;
import java.util.UUID;

public record ConversationDetailDto(UUID id, String title, List<ChatMessageDto> messages) {
}