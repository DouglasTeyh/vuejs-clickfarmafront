package com.clickfarma.backend.service;

import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.repository.ProdutoRepository;
import com.clickfarma.backend.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiRouterService {

    @Value("${ai.provider:groq}")
    private String aiProvider;

    @Autowired(required = false)
    private GeminiService geminiService;

    @Autowired(required = false)
    private GroqService groqService;

    @Autowired
    private HistoricoVisualizacaoService historicoService;

    @Autowired
    private com.clickfarma.backend.repository.ProdutoRepository produtoRepository;

    @Autowired
    private com.clickfarma.backend.repository.PedidoRepository pedidoRepository;

    public Mono<String> chat(String mensagem) {
        if ("gemini".equalsIgnoreCase(aiProvider) && geminiService != null) {
            return geminiService.chat(mensagem);
        } else if (groqService != null) {
            return groqService.chat(mensagem);
        } else {
            return Mono.just("Nenhum servico de IA disponivel");
        }
    }

    public Mono<String> chatWithHistory(List<Map<String, String>> conversacao) {
        if ("gemini".equalsIgnoreCase(aiProvider) && geminiService != null) {
            // Gemini is not updated yet, fallback to single message
            return geminiService.chat(conversacao.get(conversacao.size() - 1).get("content"));
        } else if (groqService != null) {
            return groqService.chatWithHistory(conversacao);
        } else {
            return Mono.just("Nenhum servico de IA disponivel");
        }
    }

    public Mono<String> analyzeCart(List<Map<String, Object>> cartItems, Double totalPrice) {
        if ("gemini".equalsIgnoreCase(aiProvider) && geminiService != null) {
            return geminiService.analyzeCart(cartItems, totalPrice);
        } else if (groqService != null) {
            return groqService.analyzeCart(cartItems, totalPrice);
        } else {
            return Mono.just("Servico de IA nao disponivel");
        }
    }

    public Mono<String> getWellnessSuggestions(String userId, String userName) {
        if ("gemini".equalsIgnoreCase(aiProvider) && geminiService != null) {
            return geminiService.getWellnessSuggestions(userId, userName);
        } else if (groqService != null) {
            return groqService.getWellnessSuggestions(userId, userName);
        } else {
            return Mono.just("Servico de IA nao disponivel");
        }
    }

    public Mono<List<Long>> getRecommendations(Long usuarioId) {
        List<Long> vistos = (usuarioId != null) ? historicoService.getProdutosRecentes(usuarioId) : List.of();
        
        // Buscar IDs de produtos comprados recentemente
        List<Long> comprados = (usuarioId != null) ? 
            pedidoRepository.findByUsuarioId(usuarioId).stream()
                .flatMap(p -> p.getItens().stream())
                .map(item -> item.getProduto().getId())
                .distinct()
                .limit(10)
                .collect(Collectors.toList()) : List.of();

        List<com.clickfarma.backend.model.Produto> produtos = produtoRepository.findAll().stream()
                .limit(100)
                .collect(Collectors.toList());
        
        if ("gemini".equalsIgnoreCase(aiProvider) && geminiService != null) {
            return geminiService.getRecommendations(usuarioId, vistos, comprados, produtos);
        }
        return Mono.just(List.of()); // Fallback
    }

    public Mono<String> interpretSearchQuery(String query) {
        String prompt = String.format(
            "Atue como um farmaceutico especialista. O usuario buscou por: '%s'. " +
            "Sua tarefa e converter essa busca em uma lista de palavras-chave, categorias ou principios ativos que ajudem a encontrar o produto no banco de dados. " +
            "Responda APENAS com as palavras-chave separadas por virgula. Exemplo: 'Paracetamol, Dor, Febre, Analgesico'.",
            query
        );
        return this.chat(prompt);
    }
}
