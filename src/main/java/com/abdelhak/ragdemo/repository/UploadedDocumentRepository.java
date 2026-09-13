package com.abdelhak.ragdemo.repository;

import com.abdelhak.ragdemo.entities.UploadedDocument;
import com.abdelhak.ragdemo.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadedDocumentRepository extends JpaRepository<UploadedDocument, UUID> {
    List<UploadedDocument> findByUserOrderByUploadedAtDesc(User user);
    Optional<UploadedDocument> findFirstByUserAndFilenameOrderByUploadedAtDesc(User user, String filename);
}