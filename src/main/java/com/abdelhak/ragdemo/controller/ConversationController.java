package com.abdelhak.ragdemo.controller;

import com.abdelhak.ragdemo.dto.ChatMessageDto;
import com.abdelhak.ragdemo.dto.ConversationDetailDto;
import com.abdelhak.ragdemo.dto.ConversationSummaryDto;
import com.abdelhak.ragdemo.entities.ChatMessage;
import com.abdelhak.ragdemo.entities.Conversation;
import com.abdelhak.ragdemo.entities.User;
import com.abdelhak.ragdemo.repository.ChatMessageRepository;
import com.abdelhak.ragdemo.repository.ConversationRepository;
import com.abdelhak.ragdemo.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public List<ConversationSummaryDto> listConversations() {
        User user = currentUserProvider.getCurrentUser();

        return conversationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(c -> new ConversationSummaryDto(c.getId(), c.getTitle(), c.getCreatedAt()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationDetailDto> getConversation(@PathVariable UUID id) {
        User user = currentUserProvider.getCurrentUser();

        Conversation conversation = conversationRepository.findById(id)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElse(null);

        if (conversation == null) {
            return ResponseEntity.notFound().build();
        }

        List<ChatMessageDto> messages = chatMessageRepository
                .findByConversationOrderByCreatedAtAsc(conversation)
                .stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(new ConversationDetailDto(conversation.getId(), conversation.getTitle(), messages));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable UUID id) {
        User user = currentUserProvider.getCurrentUser();

        Conversation conversation = conversationRepository.findById(id)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElse(null);

        if (conversation == null) {
            return ResponseEntity.notFound().build();
        }

        conversationRepository.delete(conversation);
        return ResponseEntity.noContent().build();
    }

    private ChatMessageDto toDto(ChatMessage message) {
        List<String> sources = message.getSources()
                .stream()
                .map(doc -> doc.getFilename())
                .collect(Collectors.toList());

        return new ChatMessageDto(
                message.getId(),
                message.getRole().name(),
                message.getContent(),
                message.getCreatedAt(),
                sources
        );
    }
}