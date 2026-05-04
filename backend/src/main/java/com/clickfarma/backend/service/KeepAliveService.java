package com.clickfarma.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

@Service
public class KeepAliveService {
    private static final Logger log = LoggerFactory.getLogger(KeepAliveService.class);
    private WebClient webClient;
    
    @Value("${app.backend.url:}")
    private String backendUrl;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder().build();
    }

    @Scheduled(fixedRate = 30000) // 30 segundos
    public void keepAlive() {
        if (backendUrl == null || backendUrl.isEmpty() || backendUrl.contains("localhost")) {
            return;
        }
        
        try {
            log.info("💓 Enviando Keep-Alive para evitar suspensão no Render: {}", backendUrl);
            webClient.get()
                    .uri(backendUrl + "/api/produtos/buscar?nome=ping")
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                        res -> log.debug("✅ Sistema acordado!"),
                        err -> log.warn("⚠️ Falha no auto-ping (isso é normal se o sistema estiver iniciando): {}", err.getMessage())
                    );
        } catch (Exception e) {
            log.error("❌ Erro ao executar agendamento de Keep-Alive: {}", e.getMessage());
        }
    }
}
