package com.abdelhak.ragdemo.dto;

import java.util.List;

public record ChatResponseDto(String answer, List<String> sources) {
}