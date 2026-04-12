package com.clickfarma.backend.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PagamentoService {

    @Value("${MERCADOPAGO_ACCESS_TOKEN}")
    private String accessToken;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("✅ Mercado Pago configurado com sucesso!");
    }

    public String criarLinkPagamento(Double valorTotal, Long pedidoId) {
        try {
            log.info("💰 Criando pagamento para pedido: {} - Valor: R$ {}", pedidoId, valorTotal);

            // Criar item da preferência
            List<PreferenceItemRequest> items = new ArrayList<>();

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Pedido ClickFarma #" + pedidoId)
                    .description("Produtos da ClickFarma")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(valorTotal))
                    .currencyId("BRL")
                    .build();
            items.add(item);

            // Configurar URLs de retorno
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(pedidoId.toString())
                    .notificationUrl("https://seu-dominio.com/api/webhook") // Para produção
                    .build();

            // Criar preferência
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            log.info("✅ Link gerado: {}", preference.getInitPoint());

            return preference.getInitPoint();

        } catch (Exception e) {
            log.error("❌ Erro ao criar pagamento: {}", e.getMessage());
            throw new RuntimeException("Erro ao gerar link de pagamento: " + e.getMessage());
        }
    }
}