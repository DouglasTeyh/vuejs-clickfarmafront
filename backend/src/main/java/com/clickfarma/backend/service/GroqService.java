package com.clickfarma.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import com.clickfarma.backend.repository.ProdutoRepository;

@Service
public class GroqService {
    private static final Logger log = LoggerFactory.getLogger(GroqService.class);

    @Value("${GROQ_API_KEY:${groq.api.key:}}")
    private String apiKey;

    private static final String SYSTEM_PROMPT = 
        "Você é o ASSISTENTE VIRTUAL DA CLICKFARMA. Atue como um farmacêutico prestativo, rápido e focado em vendas.\n\n" +
        "### DIRETRIZES DE RESPOSTA (OBRIGATÓRIO):\n" +
        "1. CONCISÃO EXTREMA: Suas respostas devem ter no máximo 2 ou 3 frases. Nunca seja prolixo.\n" +
        "2. RECOMENDAÇÃO OBRIGATÓRIA: Se o usuário relatar QUALQUER sintoma ou dor, você DEVE recomendar pelo menos 3 produtos comuns para esse fim (ex: Paracetamol, Dipirona, Ibuprofeno, etc).\n" +
        "3. FORMATO DE RECOMENDAÇÃO: Use sempre: 'Para [sintoma], recomendo: [Produto 1], [Produto 2] e [Produto 3].'\n" +
        "4. SEGURANÇA (TARJA PRETA): É terminantemente PROIBIDO sugerir medicamentos controlados ou tarja preta (Rivotril, Alprazolam, Clonazepam, etc). Diga apenas que não pode indicar esse tipo de medicação.\n" +
        "5. EMERGÊNCIA: Se o usuário citar dor no peito, falta de ar grave ou desmaio, sua única resposta deve ser: 'Isso parece uma emergência. Por favor, procure um Pronto-Socorro IMEDIATAMENTE.'\n\n" +
        "### CATÁLOGO DE REFERÊNCIA (PREFIRA ESTES):\n" +
        "- Dor/Febre: Tylenol, Dorflex, Neosaldina, Dipirona, Paracetamol.\n" +
        "- Gripe: Benegrip, Cimegripe, Tylenol Sinus.\n" +
        "- Estômago: Estomazil, Eno, Omeprazol.\n" +
        "- Outros: Losartana (Pressão), Amoxil (Antibiótico - requer receita).\n\n" +
        "### COMPORTAMENTO:\n" +
        "- O QUE FAZER SEMPRE: Ser direto, sugerir produtos da lista acima e encerrar perguntando se precisa de algo mais.\n" +
        "- O QUE NUNCA FAZER: Dar aulas de medicina, explicar causas fisiológicas longas ou alucinar substâncias ilegais.\n" +
        "- FOCO: O usuário quer comprar um remédio para o problema dele agora. Ajude-o a escolher.";

    @Autowired
    private ProdutoRepository produtoRepository;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GroqService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Mono<String> chat(String mensagem) {
        return chat(mensagem, 0.7);
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public Mono<String> chat(String mensagem, double temperature) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new IllegalStateException("GROQ_API_KEY (ou groq.api.key) não configurada"));
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("messages", List.of(Map.of("role", "user", "content", mensagem)));
        requestBody.put("temperature", temperature);

        return processarRequisicaoChat(requestBody);
    }

    public Mono<String> chatWithHistory(List<Map<String, String>> messages) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new IllegalStateException("GROQ_API_KEY não configurada"));
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        
        // Injetar System Prompt para RAG estrito
        java.util.List<Map<String, String>> messagesWithSystem = new java.util.ArrayList<>();
        messagesWithSystem.add(Map.of(
            "role", "system", 
            "content", SYSTEM_PROMPT
        ));
        messagesWithSystem.addAll(messages);
        
        requestBody.put("messages", messagesWithSystem);
        requestBody.put("temperature", 0.7);

        return processarRequisicaoChat(requestBody);
    }

    private Mono<String> processarRequisicaoChat(Map<String, Object> requestBody) {
        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        JsonNode root = objectMapper.readTree(responseBody);
                        return root.path("choices").get(0).path("message").path("content").asText();
                    } catch (Exception e) {
                        return "Erro ao processar resposta: " + e.getMessage();
                    }
                });
    }

    public Mono<String> analyzeCart(List<Map<String, Object>> cartItems, Double totalPrice) {
        String prompt = buildCartAnalysisPrompt(cartItems, totalPrice);
        return this.chat(prompt);
    }

    public Mono<String> getWellnessSuggestions(String userId, String userName) {
        String prompt = String.format(
            "Voce e um assistente de saude da ClickFarma. O usuario %s solicitou recomendacoes de bem-estar. Forneca 3 dicas praticas de saude em MARKDOWN.",
            userName != null ? userName : "Cliente"
        );
        return this.chat(prompt);
    }

    private String buildCartAnalysisPrompt(List<Map<String, Object>> cartItems, Double totalPrice) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analise o carrinho de compras e forneca sugestoes em MARKDOWN:\n\n");
        for (Map<String, Object> item : cartItems) {
            prompt.append("- ").append(item.get("name")).append("\n");
        }
        prompt.append("Total: R$ ").append(totalPrice);
        return prompt.toString();
    }

    public Mono<String> extractTextFromImage(String base64Image) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your_groq_api_key_here")) {
            return Mono.just("Erro: Chave da API Groq não configurada");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.2-90b-vision-preview");

        // Format is data:image/jpeg;base64,... 
        String dataUrl = base64Image;
        if (!dataUrl.startsWith("data:image")) {
            dataUrl = "data:image/jpeg;base64," + base64Image;
        }

        List<Map<String, Object>> messages = new java.util.ArrayList<>();
        
        List<Map<String, Object>> contentParts = new java.util.ArrayList<>();
        contentParts.add(Map.of("type", "text", "text", "Por favor, atue como um farmacêutico e leia a imagem. Extraia SOMENTE os nomes dos princípios ativos (remédios) escritos nesta receita. Responda num formato limpo delimitando por vírgula."));
        contentParts.add(Map.of("type", "image_url", "image_url", Map.of("url", dataUrl)));

        messages.add(Map.of("role", "user", "content", contentParts));
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.2);

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        JsonNode root = objectMapper.readTree(responseBody);
                        return root.path("choices").get(0).path("message").path("content").asText();
                    } catch (Exception e) {
                        return "Erro ao processar visão: " + e.getMessage();
                    }
                })
                .onErrorResume(org.springframework.web.reactive.function.client.WebClientResponseException.class, e -> {
                    log.error("Groq Vision API Error: {} - Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
                    return Mono.just("Erro: 400 Bad Request from Groq Vision API");
                });
    }
}
