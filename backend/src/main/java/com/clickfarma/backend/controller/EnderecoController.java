package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.MensagemResponseDTO;
import com.clickfarma.backend.model.Endereco;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.EnderecoRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enderecos")
public class EnderecoController {

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<?> listarMeusEnderecos(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).build();
        Usuario usuario = getUsuario(authentication);
        return ResponseEntity.ok(enderecoRepository.findByUsuarioId(usuario.getId()));
    }

    @PostMapping
    public ResponseEntity<?> adicionarEndereco(Authentication authentication, @RequestBody Endereco endereco) {
        if (authentication == null) return ResponseEntity.status(401).build();
        Usuario usuario = getUsuario(authentication);
        
        endereco.setUsuario(usuario);
        
        // Se for o primeiro endereço ou se for marcado como padrão, desmarca os outros
        List<Endereco> atuais = enderecoRepository.findByUsuarioId(usuario.getId());
        if (atuais.isEmpty()) {
            endereco.setPadrao(true);
        } else if (endereco.isPadrao()) {
            desmarcarPadroes(atuais);
        }
        
        Endereco salvo = enderecoRepository.save(endereco);
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}/padrao")
    public ResponseEntity<?> definirPadrao(Authentication authentication, @PathVariable Long id) {
        if (authentication == null) return ResponseEntity.status(401).build();
        Usuario usuario = getUsuario(authentication);
        
        List<Endereco> enderecos = enderecoRepository.findByUsuarioId(usuario.getId());
        desmarcarPadroes(enderecos);
        
        Endereco alvo = enderecoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"));
        
        if (!alvo.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.status(403).body(new MensagemResponseDTO("Acesso negado", false));
        }
        
        alvo.setPadrao(true);
        enderecoRepository.save(alvo);
        
        return ResponseEntity.ok(new MensagemResponseDTO("Endereço padrão atualizado", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarEndereco(Authentication authentication, @PathVariable Long id) {
        if (authentication == null) return ResponseEntity.status(401).build();
        Usuario usuario = getUsuario(authentication);
        
        Endereco alvo = enderecoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"));
        
        if (!alvo.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.status(403).body(new MensagemResponseDTO("Acesso negado", false));
        }
        
        if (alvo.isPadrao()) {
            return ResponseEntity.badRequest().body(new MensagemResponseDTO("Não é possível deletar o endereço padrão", false));
        }
        
        enderecoRepository.delete(alvo);
        return ResponseEntity.ok(new MensagemResponseDTO("Endereço removido", true));
    }

    private Usuario getUsuario(Authentication authentication) {
        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private void desmarcarPadroes(List<Endereco> enderecos) {
        enderecos.forEach(e -> {
            if (e.isPadrao()) {
                e.setPadrao(false);
                enderecoRepository.save(e);
            }
        });
    }
}
