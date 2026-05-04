package com.clickfarma.backend.service;

import com.clickfarma.backend.model.Farmacia;
import com.clickfarma.backend.model.Motoboy;
import com.clickfarma.backend.model.Pagamento;
import com.clickfarma.backend.repository.FarmaciaRepository;
import com.clickfarma.backend.repository.MotoboyRepository;
import com.clickfarma.backend.repository.PagamentoRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PagamentoService {

    private static final Logger log = LoggerFactory.getLogger(PagamentoService.class);

    @Value("${MERCADOPAGO_ACCESS_TOKEN:}")
    private String accessToken;

    @Value("${app.frontend.url:http://localhost:8081}")
    private String frontendUrl;

    @Value("${app.webhook.url:}")
    private String webhookUrl;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private FarmaciaRepository farmaciaRepository;

    @Autowired
    private MotoboyRepository motoboyRepository;

    @PostConstruct
    public void init() {
        if (accessToken == null || accessToken.isBlank()) {
            log.warn("MERCADOPAGO_ACCESS_TOKEN nao configurado. O backend iniciara, mas a criacao de links de pagamento ficara desabilitada.");
            return;
        }

        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("✅ Mercado Pago configurado com sucesso!");
    }

    public String criarLinkPagamento(Double valorTotal, Long pedidoId) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("Mercado Pago nao configurado. Defina MERCADOPAGO_ACCESS_TOKEN para habilitar pagamentos.");
        }

        try {
            log.info("💰 Criando pagamento para pedido: {} - Valor: R$ {}", pedidoId, valorTotal);

            PreferenceClient client = new PreferenceClient();

            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title("Pedido ClickFarma #" + pedidoId)
                    .quantity(1)
                    .unitPrice(new BigDecimal(valorTotal))
                    .currencyId("BRL")
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            String baseFront = frontendUrl != null ? frontendUrl.replaceAll("/+$", "") : "http://localhost:8081";
            String successUrl = baseFront + "/sucesso-pagamento";
            String pendingUrl = baseFront + "/order-confirmation";
            String failureUrl = baseFront + "/checkout";

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .pending(pendingUrl)
                    .failure(failureUrl)
                    .build();

            String notification = normalizeNotificationUrl(webhookUrl);

            String autoReturn = (successUrl != null && successUrl.toLowerCase().startsWith("https://"))
                    ? "approved"
                    : null;

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(pedidoId.toString())
                    .backUrls(backUrls)
                    .autoReturn(autoReturn)
                    .notificationUrl(notification)
                    .build();

            Preference preference = client.create(preferenceRequest);
            return preference.getInitPoint();

        } catch (MPApiException e) {
            log.error("❌ Erro ao criar pagamento (MPApiException). status={} content={}",
                    e.getStatusCode(), e.getApiResponse() != null ? e.getApiResponse().getContent() : "null");
            throw new RuntimeException("Erro ao gerar link de pagamento (Mercado Pago): " +
                    (e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage()));
        } catch (MPException e) {
            log.error("❌ Erro ao criar pagamento (MPException): {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar link de pagamento (Mercado Pago): " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Erro ao criar pagamento: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar link de pagamento: " + e.getMessage());
        }
    }

    public List<Pagamento> listarTodos() {
        return pagamentoRepository.findAll();
    }

    public Optional<Pagamento> buscarPorId(Long id) {
        return pagamentoRepository.findById(id);
    }
    
    public List<Pagamento> listarPendentes() {
        return pagamentoRepository.findByStatus(Pagamento.StatusPagamento.PENDENTE);
    }

    public List<Pagamento> listarPorFarmacia(Long farmaciaId) {
        return pagamentoRepository.findByFarmaciaId(farmaciaId);
    }

    public List<Pagamento> listarPorMotoboy(Long motoboyId) {
        return pagamentoRepository.findByMotoboyId(motoboyId);
    }

    @Transactional
    public Pagamento marcarComoPago(Long id, String observacoes) {
        Pagamento p = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
        p.setStatus(Pagamento.StatusPagamento.PAGO);
        p.setDataPagamento(LocalDateTime.now());
        p.setObservacoes(observacoes);
        return pagamentoRepository.save(p);
    }

    @Transactional
    public Map<String, Object> gerarPagamentoFarmacia(Long farmaciaId, String periodo) {
        Farmacia farmacia = farmaciaRepository.findById(farmaciaId)
                .orElseThrow(() -> new RuntimeException("Farmácia não encontrada"));

        // Lógica simplificada para exemplo, ajuste conforme necessário
        BigDecimal valorBruto = BigDecimal.ZERO; 
        BigDecimal taxa = BigDecimal.ZERO;
        BigDecimal liquido = BigDecimal.ZERO;

        Pagamento p = new Pagamento();
        p.setTipo(Pagamento.TipoPagamento.FARMACIA);
        p.setFarmacia(farmacia);
        p.setValorBruto(valorBruto);
        p.setValorTaxa(taxa);
        p.setValorLiquido(liquido);
        p.setChavePix(farmacia.getChavePix());
        p.setReferenciaPeriodo(periodo);
        p.setStatus(Pagamento.StatusPagamento.PENDENTE);
        p.setDataCriacao(LocalDateTime.now());

        Pagamento saved = pagamentoRepository.save(p);
        Map<String, Object> result = new HashMap<>();
        result.put("id", saved.getId());
        result.put("status", saved.getStatus());
        return result;
    }

    @Transactional
    public Map<String, Object> gerarPagamentoMotoboy(Long motoboyId, String periodo) {
        Motoboy motoboy = motoboyRepository.findById(motoboyId)
                .orElseThrow(() -> new RuntimeException("Motoboy não encontrado"));

        Pagamento p = new Pagamento();
        p.setTipo(Pagamento.TipoPagamento.MOTOBOY);
        p.setMotoboy(motoboy);
        p.setValorBruto(BigDecimal.ZERO);
        p.setValorTaxa(BigDecimal.ZERO);
        p.setValorLiquido(BigDecimal.ZERO);
        p.setChavePix(motoboy.getChavePix());
        p.setReferenciaPeriodo(periodo);
        p.setStatus(Pagamento.StatusPagamento.PENDENTE);
        p.setDataCriacao(LocalDateTime.now());

        Pagamento saved = pagamentoRepository.save(p);
        Map<String, Object> result = new HashMap<>();
        result.put("id", saved.getId());
        result.put("status", saved.getStatus());
        return result;
    }

    private String normalizeNotificationUrl(String raw) {
        if (raw == null) return null;
        String url = raw.trim();
        if (url.isBlank()) return null;
        String lower = url.toLowerCase();
        if (lower.startsWith("http://localhost")
                || lower.startsWith("http://127.0.0.1")
                || lower.startsWith("http://0.0.0.0")
                || lower.startsWith("https://localhost")
                || lower.startsWith("https://127.0.0.1")
                || lower.startsWith("https://0.0.0.0")) {
            return null;
        }
        if (!lower.startsWith("https://")) {
            return null;
        }
        return url;
    }
}
