package com.clickfarma.backend.controller;

import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.model.SacolaItem;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.ProdutoRepository;
import com.clickfarma.backend.repository.SacolaItemRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.clickfarma.backend.dto.SacolaItemResponseDTO;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sacola")
public class SacolaController {

    @Autowired
    private SacolaItemRepository sacolaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<SacolaItemResponseDTO>> listar(@PathVariable Long usuarioId) {
        List<SacolaItemResponseDTO> dtos = sacolaRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(SacolaItemResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionar(@RequestBody Map<String, Object> body) {
        Long usuarioId = ((Number) body.get("usuarioId")).longValue();
        Long produtoId = ((Number) body.get("produtoId")).longValue();
        Integer qtd = (Integer) body.getOrDefault("quantidade", 1);

        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        Produto produto = produtoRepository.findById(produtoId).orElseThrow();

        SacolaItem item = sacolaRepository.findByUsuarioIdAndProdutoId(usuarioId, produtoId)
                .orElse(new SacolaItem(usuario, produto, 0));
        
        item.setQuantidade(item.getQuantidade() + qtd);
        return ResponseEntity.ok(sacolaRepository.save(item));
    }

    @DeleteMapping("/remover/{id}")
    public ResponseEntity<?> remover(@PathVariable Long id) {
        sacolaRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/limpar/{usuarioId}")
    @Transactional
    public ResponseEntity<?> limpar(@PathVariable Long usuarioId) {
        sacolaRepository.deleteByUsuarioId(usuarioId);
        return ResponseEntity.ok().build();
    }
}
