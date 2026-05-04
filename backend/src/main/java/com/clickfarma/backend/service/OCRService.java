package com.clickfarma.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.awt.image.BufferedImage;

@Service
public class OCRService {
    private static final Logger log = LoggerFactory.getLogger(OCRService.class);

    @Value("${ocr.space.api.key}")
    private String apiKey;

    @Value("${ocr.space.api.url}")
    private String apiUrl;

    @Value("${ocr.space.language:por}")
    private String language;

    @Value("${ocr.space.isTable:false}")
    private boolean isTable;

    @Value("${ocr.space.preprocess:true}")
    private boolean preprocessImage;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OCRService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    public Mono<String> extrairTextoDeImagem(String imagemBase64) {
        log.info("Iniciando OCR na imagem via OCR Space API");

        String activeApiKey = (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your_ocr_api_key_here")) ? apiKey : "helloworld";

        String imagemLimpa = imagemBase64;
        if (imagemBase64.contains(",")) {
            imagemLimpa = imagemBase64.substring(imagemBase64.indexOf(",") + 1);
        }

        String payloadBase64 = imagemLimpa;
        String imageDataUriPrefix = "data:image/jpeg;base64,";

        if (preprocessImage) {
            try {
                BufferedImage decoded = OcrImageEnhancer.decodeBase64(imagemLimpa);
                if (decoded != null) {
                    BufferedImage enhanced = OcrImageEnhancer.enhanceForHandwritingOcr(decoded);
                    payloadBase64 = OcrImageEnhancer.encodePngBase64(enhanced);
                    imageDataUriPrefix = "data:image/png;base64,";
                    log.debug("Imagem pré-processada para OCR.space (PNG, contraste/upscale)");
                }
            } catch (Exception e) {
                log.warn("Pré-processamento da imagem para OCR.space falhou; usando original: {}", e.getMessage());
            }
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("apikey", activeApiKey);
        formData.add("base64Image", imageDataUriPrefix + payloadBase64);
        formData.add("language", language);
        formData.add("isOverlayRequired", "false");
        formData.add("detectOrientation", "true");
        formData.add("scale", "true");
        formData.add("isTable", String.valueOf(isTable));
        formData.add("OCREngine", "2");

        return webClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Accept", "application/json")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        JsonNode root = objectMapper.readTree(response);
                        if (root.has("OCRExitCode") && root.get("OCRExitCode").asInt() != 1) {
                            String errorMessage = "Erro desconhecido";
                            if (root.has("ErrorMessage") && !root.get("ErrorMessage").isNull() && root.get("ErrorMessage").isArray()) {
                                errorMessage = root.get("ErrorMessage").get(0).asText();
                            }
                            log.error("Erro retornado pela API OCR Space: {}", errorMessage);
                            return "Erro OCR Space: " + errorMessage;
                        }

                        JsonNode parsedResults = root.get("ParsedResults");
                        if (parsedResults != null && parsedResults.isArray() && parsedResults.size() > 0) {
                            String textoExtraido = parsedResults.get(0)
                                    .get("ParsedText")
                                    .asText();

                            if (textoExtraido == null || textoExtraido.trim().isEmpty()) {
                                log.warn("API retornou sucesso, mas nenhum texto foi extraído.");
                                return "Nenhum texto identificado na imagem.";
                            }

                            log.info("Texto extraído com sucesso ({} caracteres)", textoExtraido.length());
                            return textoExtraido;
                        }
                        return "Nenhum resultado processado encontrado.";
                    } catch (Exception e) {
                        log.error("Erro ao processar JSON de resposta do OCR Space", e);
                        return "Erro ao processar resposta: " + e.getMessage();
                    }
                })
                .onErrorResume(e -> {
                    log.error("Falha na comunicação com OCR Space API", e);
                    return Mono.just("Erro de conexão: " + e.getMessage());
                });
    }
}
