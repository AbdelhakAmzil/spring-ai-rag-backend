package com.abdelhak.ragdemo.repository;

import com.abdelhak.ragdemo.entities.Conversation;
import com.abdelhak.ragdemo.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByUserOrderByCreatedAtDesc(User user);
}