package com.abdelhak.ragdemo.controller;

import com.abdelhak.ragdemo.dto.ChatRequest;
import com.abdelhak.ragdemo.dto.ChatResponseDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping
    public ChatResponseDto ask(@RequestBody ChatRequest request) {
        var chatResponse = chatClient.prompt()
                .user(request.question())
                .call()
                .chatResponse();

        String answer = chatResponse.getResult().getOutput().getText();

        @SuppressWarnings("unchecked")
        List<Document> retrievedDocuments = (List<Document>) chatResponse.getMetadata()
                .get("rag_document_context");

        List<String> sources = retrievedDocuments == null
                ? List.of()
                : retrievedDocuments.stream()
                  .map(doc -> (String) doc.getMetadata().getOrDefault("source", "unknown"))
                  .collect(Collectors.toCollection(java.util.LinkedHashSet::new))
                  .stream()
                  .toList();

        return new ChatResponseDto(answer, sources);
    }
}