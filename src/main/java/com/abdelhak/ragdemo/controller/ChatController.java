package com.abdelhak.ragdemo.controller;

import com.abdelhak.ragdemo.dto.ChatRequest;
import com.abdelhak.ragdemo.dto.ChatResponseDto;
import com.abdelhak.ragdemo.entities.ChatMessage;
import com.abdelhak.ragdemo.entities.Conversation;
import com.abdelhak.ragdemo.entities.Role;
import com.abdelhak.ragdemo.entities.UploadedDocument;
import com.abdelhak.ragdemo.entities.User;
import com.abdelhak.ragdemo.repository.ChatMessageRepository;
import com.abdelhak.ragdemo.repository.ConversationRepository;
import com.abdelhak.ragdemo.repository.UploadedDocumentRepository;
import com.abdelhak.ragdemo.security.CurrentUserProvider;
import com.abdelhak.ragdemo.service.IntentClassifier;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UploadedDocumentRepository uploadedDocumentRepository;
    private final CurrentUserProvider currentUserProvider;
    private final IntentClassifier intentClassifier;
    private final String chatModelName;

    @PostMapping
    public ChatResponseDto ask(@RequestBody ChatRequest request) {
        User user = currentUserProvider.getCurrentUser();

        Conversation conversation = resolveConversation(request, user);

        saveMessage(conversation, Role.USER, request.question(), Set.of());

        long startTime = System.currentTimeMillis();

        var chatResponse = chatClient.prompt()
                .user(request.question())
                .call()
                .chatResponse();

        long responseTimeMs = System.currentTimeMillis() - startTime;

        String answer = chatResponse.getResult().getOutput().getText();

        @SuppressWarnings("unchecked")
        List<Document> retrievedDocuments = (List<Document>) chatResponse.getMetadata()
                .get("rag_document_context");

        int retrievedChunks = retrievedDocuments == null ? 0 : retrievedDocuments.size();

        List<String> sources = retrievedDocuments == null
                ? List.of()
                : retrievedDocuments.stream()
                  .map(doc -> (String) doc.getMetadata().getOrDefault("source", "unknown"))
                  .collect(Collectors.toCollection(LinkedHashSet::new))
                  .stream()
                  .toList();

        Set<UploadedDocument> linkedSources = sources.stream()
                .map(filename -> uploadedDocumentRepository
                        .findFirstByUserAndFilenameOrderByUploadedAtDesc(user, filename)
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        saveMessage(conversation, Role.ASSISTANT, answer, linkedSources);

        String intent = intentClassifier.classify(request.question());

        return new ChatResponseDto(answer, sources, conversation.getId(), responseTimeMs, retrievedChunks, intent, chatModelName);
    }

    private Conversation resolveConversation(ChatRequest request, User user) {
        if (request.conversationId() != null) {
            return conversationRepository.findById(request.conversationId())
                    .filter(c -> c.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        }

        Conversation conversation = new Conversation();
        conversation.setUser(user);
        conversation.setTitle(buildTitle(request.question()));
        return conversationRepository.save(conversation);
    }

    private String buildTitle(String question) {
        String trimmed = question.strip();
        return trimmed.length() > 60 ? trimmed.substring(0, 60) + "..." : trimmed;
    }

    private void saveMessage(Conversation conversation, Role role, String content, Set<UploadedDocument> sources) {
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setRole(role);
        message.setContent(content);
        message.setSources(sources);
        chatMessageRepository.save(message);
    }
}