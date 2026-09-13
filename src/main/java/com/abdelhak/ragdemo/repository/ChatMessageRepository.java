package com.abdelhak.ragdemo.repository;

import com.abdelhak.ragdemo.entities.ChatMessage;
import com.abdelhak.ragdemo.entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findByConversationOrderByCreatedAtAsc(Conversation conversation);
}