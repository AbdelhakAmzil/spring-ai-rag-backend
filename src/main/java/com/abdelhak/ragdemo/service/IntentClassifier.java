package com.abdelhak.ragdemo.service;

import org.springframework.stereotype.Service;

@Service
public class IntentClassifier {

    public String classify(String question) {
        String q = question.strip().toLowerCase();

        if (q.startsWith("summarize") || q.startsWith("résume") || q.contains("summary")) {
            return "Summarization";
        }
        if (q.startsWith("compare") || q.contains(" vs ") || q.contains("difference between")) {
            return "Comparison";
        }
        if (q.startsWith("how to") || q.startsWith("comment faire") || q.startsWith("explain how")) {
            return "How-to";
        }
        if (q.startsWith("what") || q.startsWith("who") || q.startsWith("why") || q.startsWith("when")
                || q.startsWith("where") || q.startsWith("quel") || q.startsWith("qui")
                || q.startsWith("pourquoi") || q.startsWith("quand") || q.startsWith("où")
                || q.startsWith("c'est quoi") || q.startsWith("qu'est-ce")) {
            return "Informational";
        }

        return "General";
    }
}