package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.MensagemResponseDTO;
import com.clickfarma.backend.dto.UsuarioRequestDTO;
import com.clickfarma.backend.dto.UsuarioResponseDTO;
import com.clickfarma.backend.model.Farmacia;
import com.clickfarma.backend.model.Motoboy;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.FarmaciaRepository;
import com.clickfarma.backend.repository.MotoboyRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import com.clickfarma.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private FarmaciaRepository farmaciaRepository;

    @Autowired
    private MotoboyRepository motoboyRepository;

    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(new MensagemResponseDTO("Não autenticado", false));
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        UsuarioResponseDTO response = new UsuarioResponseDTO(usuario);

        // Se for Farmácia, adiciona dados extras
        if ("PHARMACY".equals(usuario.getRole())) {
            farmaciaRepository.findByUsuarioId(usuario.getId()).ifPresent(f -> {
                response.setCnpj(f.getCnpj());
                response.setChavePix(f.getChavePix());
            });
        } 
        // Se for Motoboy, adiciona dados extras
        else if ("COURIER".equals(usuario.getRole())) {
            motoboyRepository.findByUsuarioId(usuario.getId()).ifPresent(m -> {
                response.setChavePix(m.getChavePix());
            });
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(Authentication authentication, @Valid @RequestBody UsuarioRequestDTO req) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(new MensagemResponseDTO("Não autenticado", false));
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Atualiza dados básicos via service (que já trata imutabilidade de email/cpf)
        UsuarioResponseDTO updated = usuarioService.atualizarUsuario(usuario.getId(), req);

        // Atualiza dados específicos de Farmácia/Motoboy
        if ("PHARMACY".equals(usuario.getRole())) {
            farmaciaRepository.findByUsuarioId(usuario.getId()).ifPresent(f -> {
                f.setChavePix(req.getChavePix());
                farmaciaRepository.save(f);
                updated.setChavePix(f.getChavePix());
                updated.setCnpj(f.getCnpj());
            });
        } else if ("COURIER".equals(usuario.getRole())) {
            motoboyRepository.findByUsuarioId(usuario.getId()).ifPresent(m -> {
                m.setChavePix(req.getChavePix());
                motoboyRepository.save(m);
                updated.setChavePix(m.getChavePix());
            });
        }

        return ResponseEntity.ok(new MensagemResponseDTO("Perfil atualizado com sucesso!", true, updated));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody java.util.Map<String, String> payload) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(new MensagemResponseDTO("Não autenticado", false));
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String currentPassword = payload.get("currentPassword");
        String newPassword = payload.get("newPassword");

        try {
            usuarioService.alterarSenha(usuario.getId(), currentPassword, newPassword);
            return ResponseEntity.ok(new MensagemResponseDTO("Senha alterada com sucesso!", true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }
}
