package com.abdelhak.ragdemo.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Minimal ETL pipeline (Extract -> Transform -> Load) as described in:
 * https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html
 *
 * Extract : TikaDocumentReader (handles PDF, DOCX, TXT, HTML, ...)
 * Transform: TokenTextSplitter (chunking so retrieval returns focused passages)
 * Load    : VectorStore.add() (embeds each chunk via the Ollama embedding model, stores in PGVector)
 */
@Service
public class DocumentIngestionService {

    private final VectorStore vectorStore;

    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public int ingestFile(String fileName, byte[] content) {
        Resource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> rawDocuments = reader.read();

        TokenTextSplitter splitter = TokenTextSplitter.builder().build();
        List<Document> chunks = splitter.apply(rawDocuments);

        // Tag every chunk with its source file so you can later filter retrieval
        // via VectorStoreDocumentRetriever.filterExpression(), e.g. source == 'foo.pdf'
        chunks.forEach(doc -> doc.getMetadata().put("source", fileName));

        vectorStore.add(chunks);
        return chunks.size();
    }

    /** Convenience method for ingesting raw text without a file (e.g. from a REST body). */
    public int ingestText(String sourceLabel, String text) {
        Document document = new Document(text, Map.of("source", sourceLabel));
        TokenTextSplitter splitter = TokenTextSplitter.builder().build();
        List<Document> chunks = splitter.apply(List.of(document));
        vectorStore.add(chunks);
        return chunks.size();
    }
}
