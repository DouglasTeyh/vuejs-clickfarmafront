package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.ReceitaRequestDTO;
import com.clickfarma.backend.service.TesseractOCRService;
import com.clickfarma.backend.service.GroqProcessadorReceitaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/receita")
@CrossOrigin(origins = {"http://localhost", "http://localhost:8080", "http://localhost:8081"})
@Slf4j
public class ReceitaController {

    @Autowired
    private TesseractOCRService tesseractOCRService;

    @Autowired
    private GroqProcessadorReceitaService groqProcessadorReceitaService;

    @PostMapping("/processar")
    public Mono<ResponseEntity<Map<String, Object>>> processarReceita(@RequestBody ReceitaRequestDTO request) {
        log.info("Recebida requisição para processar receita: {}", request.getNomeArquivo());

        if (request.getImagemBase64() == null || request.getImagemBase64().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("sucesso", false);
            errorResponse.put("erro", "Imagem não fornecida");
            return Mono.just(ResponseEntity.badRequest().body(errorResponse));
        }

        // Usar Tesseract OCR
        String textoExtraido = tesseractOCRService.extrairTexto(request.getImagemBase64());

        // Verificar se houve erro no OCR
        if (textoExtraido.startsWith("Erro") || textoExtraido.equals("Nenhum texto encontrado na imagem")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("sucesso", false);
            errorResponse.put("erro", textoExtraido);
            return Mono.just(ResponseEntity.ok(errorResponse));
        }

        return groqProcessadorReceitaService.processarReceita(textoExtraido)
                .map(medicamentos -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("sucesso", true);
                    response.put("medicamentos", medicamentos.getMedicamentos());
                    response.put("textoOriginal", medicamentos.getTextoOriginal());
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(erro -> {
                    log.error("Erro no processamento da receita", erro);
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("sucesso", false);
                    errorResponse.put("erro", "Erro interno: " + erro.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().body(errorResponse));
                });
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Tesseract OCR OK");
        response.put("service", "Tesseract OCR - Offline");
        return ResponseEntity.ok(response);
    }
}