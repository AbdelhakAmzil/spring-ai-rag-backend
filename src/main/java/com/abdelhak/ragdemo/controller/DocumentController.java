package com.abdelhak.ragdemo.controller;

import com.abdelhak.ragdemo.dto.IngestResponse;
import com.abdelhak.ragdemo.dto.UploadedDocumentDto;
import com.abdelhak.ragdemo.entities.UploadedDocument;
import com.abdelhak.ragdemo.entities.User;
import com.abdelhak.ragdemo.repository.UploadedDocumentRepository;
import com.abdelhak.ragdemo.security.CurrentUserProvider;
import com.abdelhak.ragdemo.service.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentIngestionService ingestionService;
    private final UploadedDocumentRepository uploadedDocumentRepository;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public IngestResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
        User user = currentUserProvider.getCurrentUser();
        String filename = file.getOriginalFilename();
        int chunks = ingestionService.ingestFile(filename, file.getBytes(), user.getId());

        saveUploadedDocument(user, filename, chunks);

        return new IngestResponse(filename, chunks);
    }

    @PostMapping("/text")
    public IngestResponse ingestText(@RequestBody Map<String, String> body) {
        User user = currentUserProvider.getCurrentUser();
        String source = body.getOrDefault("source", "inline-text");
        String text = body.get("text");
        int chunks = ingestionService.ingestText(source, text, user.getId());

        saveUploadedDocument(user, source, chunks);

        return new IngestResponse(source, chunks);
    }

    @GetMapping
    public List<UploadedDocumentDto> listDocuments() {
        User user = currentUserProvider.getCurrentUser();

        return uploadedDocumentRepository.findByUserOrderByUploadedAtDesc(user)
                .stream()
                .map(doc -> new UploadedDocumentDto(doc.getFilename(), doc.getChunksIndexed(), doc.getUploadedAt()))
                .toList();
    }

    private void saveUploadedDocument(User user, String filename, int chunks) {
        UploadedDocument doc = new UploadedDocument();
        doc.setUser(user);
        doc.setFilename(filename);
        doc.setChunksIndexed(chunks);
        uploadedDocumentRepository.save(doc);
    }
}