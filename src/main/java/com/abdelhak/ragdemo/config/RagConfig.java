package com.abdelhak.ragdemo.config;

import com.abdelhak.ragdemo.entities.User;
import com.abdelhak.ragdemo.repository.UserRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.Supplier;

@Configuration
public class RagConfig {

    @Bean
    public String chatModelName(@org.springframework.beans.factory.annotation.Value("${spring.ai.ollama.chat.model}") String modelName) {
        return modelName;
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel, VectorStore vectorStore, UserRepository userRepository) {

        Supplier<Filter.Expression> currentUserFilter = () -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication != null ? (String) authentication.getPrincipal() : null;

            User user = email != null ? userRepository.findByEmail(email).orElse(null) : null;

            // No authenticated user resolved: filter on a value that matches nothing,
            // rather than falling back to an unfiltered (cross-user) search.
            String userId = user != null ? user.getId().toString() : "unauthenticated";

            return new FilterExpressionBuilder().eq("userId", userId).build();
        };

        var documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.50)
                .topK(6)
                .filterExpression(currentUserFilter)
                .build();

        var queryAugmenter = ContextualQueryAugmenter.builder()
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
}