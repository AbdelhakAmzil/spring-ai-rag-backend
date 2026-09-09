package com.abdelhak.ragdemo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires up two flavors of RAG, both driven by the same PGVector VectorStore:
 *
 *  1) "simpleChatClient" -> QuestionAnswerAdvisor      (naive RAG, one-shot similarity search + prompt stuffing)
 *  2) "chatClient"        -> RetrievalAugmentationAdvisor (modular RAG: retriever + contextual query augmenter,
 *                                                          easy to extend with query transformers/expanders later)
 *
 * See: https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html
 */
@Configuration
public class RagConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel, VectorStore vectorStore) {

        var documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.50)
                .topK(6)
                .build();

        var queryAugmenter = ContextualQueryAugmenter.builder()
                // Instead of refusing to answer, we allow the model to fall back on general
                // knowledge if nothing relevant was retrieved. Flip to false for stricter RAG.
                .allowEmptyContext(true)
                .build();

        var retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(queryAugmenter)
                .build();

        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        You are a helpful assistant answering questions strictly using the
                        provided context when it is relevant. If the context does not contain
                        the answer, say so clearly instead of making things up.
                        """)
                .defaultAdvisors(retrievalAugmentationAdvisor)
                .build();
    }

    /**
     * Simpler alternative kept for reference / comparison: the naive QuestionAnswerAdvisor
     * flow described first in the Spring AI docs. Not exposed as a REST endpoint by default,
     * but you can inject it wherever you want to compare behavior against the advanced flow.
     */
    @Bean
    public ChatClient simpleChatClient(ChatModel chatModel, VectorStore vectorStore) {
        var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .build();

        return ChatClient.builder(chatModel)
                .defaultAdvisors(qaAdvisor)
                .build();
    }
}
