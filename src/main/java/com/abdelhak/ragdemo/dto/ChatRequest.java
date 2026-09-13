package com.abdelhak.ragdemo.dto;

import java.util.UUID;

public record ChatRequest(String question, UUID conversationId) {
}