package com.clickfarma.backend.service;

import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    public void enviarEmail(String para, String assunto, String corpo) {
        log.info("Simulando envio de email para: {} | Assunto: {}", para, assunto);
        // Implementação real de JavaMailSender viria aqui
    }

    public void notificarNovoPedido(String email, Long pedidoId) {
        enviarEmail(email, "Novo Pedido ClickFarma #" + pedidoId, 
            "Seu pedido foi recebido com sucesso e está sendo processado.");
    }

    public void enviarConfirmacaoPedido(Usuario usuario, Pedido pedido, String linkPagamento) {
        String corpo = String.format("Olá %s, seu pedido #%d foi recebido!%s",
                usuario.getNome(), pedido.getId(), 
                linkPagamento != null ? "\nLink para pagamento: " + linkPagamento : "");
        enviarEmail(usuario.getEmail(), "Confirmação de Pedido ClickFarma #" + pedido.getId(), corpo);
    }

    public void enviarAtualizacaoStatusPedido(Usuario usuario, Pedido pedido) {
        String corpo = String.format("Olá %s, o status do seu pedido #%d foi atualizado para: %s",
                usuario.getNome(), pedido.getId(), pedido.getStatus());
        enviarEmail(usuario.getEmail(), "Atualização de Status - Pedido #" + pedido.getId(), corpo);
    }

    public boolean enviarLembreteRecompra(com.clickfarma.backend.model.AgendamentoRecompra agendamento) {
        try {
            String corpo = String.format("Olá %s, seu medicamento %s está acabando. Deseja comprar novamente?",
                    agendamento.getUsuario().getNome(), agendamento.getProduto().getNome());
            enviarEmail(agendamento.getUsuario().getEmail(), "Lembrete de Recompra - ClickFarma", corpo);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void enviarLinkRedefinicaoSenha(String email, String nome, String link) {
        String corpo = String.format("Olá %s,\n\nVocê solicitou a redefinição de sua senha no ClickFarma.\n" +
                "Clique no link abaixo para criar uma nova senha:\n%s\n\n" +
                "Este link expira em 1 hora.\n" +
                "Se você não solicitou isso, ignore este e-mail.", nome, link);
        enviarEmail(email, "Redefinição de Senha - ClickFarma", corpo);
    }
}
