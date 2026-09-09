package com.abdelhak.ragdemo.controller;

import com.abdelhak.ragdemo.dto.IngestResponse;
import com.abdelhak.ragdemo.service.DocumentIngestionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentIngestionService ingestionService;

    public DocumentController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /** Upload a PDF/DOCX/TXT/HTML file to be chunked, embedded, and stored in PGVector. */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public IngestResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
        int chunks = ingestionService.ingestFile(file.getOriginalFilename(), file.getBytes());
        return new IngestResponse(file.getOriginalFilename(), chunks);
    }

    /** Ingest raw text directly (handy for quick tests without a file). */
    @PostMapping("/text")
    public IngestResponse ingestText(@RequestBody Map<String, String> body) {
        String source = body.getOrDefault("source", "inline-text");
        String text = body.get("text");
        int chunks = ingestionService.ingestText(source, text);
        return new IngestResponse(source, chunks);
    }
}
