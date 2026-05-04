package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.TelegramLinkResponseDTO;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TelegramIntegrationService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${telegram.bot.username:ClickFarmaBot}")
    private String botUsername;

    public TelegramLinkResponseDTO gerarLinkVinculacao(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String token = UUID.randomUUID().toString();
        usuario.setTelegramLinkToken(token);
        usuario.setTelegramLinkExpiresAt(LocalDateTime.now().plusHours(1));
        usuarioRepository.save(usuario);

        String url = String.format("https://t.me/%s?start=%s", botUsername, token);
        return new TelegramLinkResponseDTO(url, token);
    }
}
