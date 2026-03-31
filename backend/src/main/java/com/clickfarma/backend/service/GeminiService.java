package com.clickfarma.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final WebClient webClient;

    public GeminiService() {
        this.webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta")
            .build();
    }

    public Mono<String> chat(String mensagem) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.just("⚠️ Chave da API Gemini não configurada. Configure a variável GEMINI_API_KEY.");
        }

        Map<String, Object> request = new HashMap<>();
        
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(
            Map.of("text", mensagem)
        ));
        
        request.put("contents", List.of(content));

        return webClient.post()
            .uri("/models/gemini-2.0-flash:generateContent?key=" + apiKey)
            .header("Content-Type", "application/json")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> {
                try {
                    List<Map<String, Object>> candidates = 
                        (List<Map<String, Object>>) response.get("candidates");
                    if (candidates != null && !candidates.isEmpty()) {
                        Map<String, Object> candidate = candidates.get(0);
                        Map<String, Object> contentResp = 
                            (Map<String, Object>) candidate.get("content");
                        List<Map<String, Object>> parts = 
                            (List<Map<String, Object>>) contentResp.get("parts");
                        return (String) parts.get(0).get("text");
                    }
                    return "Desculpe, não consegui processar sua mensagem.";
                } catch (Exception e) {
                    return "Erro ao processar resposta: " + e.getMessage();
                }
            })
            .onErrorResume(e -> {
                System.err.println("❌ Erro: " + e.getMessage());
                return Mono.just("Desculpe, estou com problemas técnicos.");
            });
    }
}
