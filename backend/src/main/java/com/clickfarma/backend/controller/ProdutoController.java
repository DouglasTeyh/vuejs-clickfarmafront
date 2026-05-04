package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.ProdutoRequestDTO;
import com.clickfarma.backend.dto.ProdutoResponseDTO;
import com.clickfarma.backend.dto.MensagemResponseDTO;
import com.clickfarma.backend.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

import com.clickfarma.backend.service.FileStorageService;
import com.clickfarma.backend.repository.FarmaciaRepository;
import com.clickfarma.backend.model.Farmacia;
import org.springframework.web.multipart.MultipartFile;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FarmaciaRepository farmaciaRepository;

    // POST - Upload de imagem do produto
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<?> uploadImagem(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            ProdutoResponseDTO produtoDTO = produtoService.buscarPorId(id);
            if (produtoDTO.getFarmaciaId() == null) {
                return ResponseEntity.badRequest().body(new MensagemResponseDTO("Produto sem farmácia vinculada", false));
            }

            Farmacia farmacia = farmaciaRepository.findById(produtoDTO.getFarmaciaId())
                    .orElseThrow(() -> new RuntimeException("Farmácia não encontrada"));

            String imageUrl = fileStorageService.salvarImagemProduto(file, farmacia.getNome());
            
            // Atualizar o produto com a nova URL
            ProdutoRequestDTO updateDTO = new ProdutoRequestDTO();
            updateDTO.setImageUrl(imageUrl);
            produtoService.atualizarProduto(id, updateDTO);

            return ResponseEntity.ok(new MensagemResponseDTO("Imagem enviada com sucesso!", true, imageUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensagemResponseDTO("Erro ao enviar imagem: " + e.getMessage(), false));
        }
    }

    // POST - Criar novo produto
    @PostMapping
    public ResponseEntity<?> criarProduto(@Valid @RequestBody ProdutoRequestDTO produtoDTO) {
        try {
            ProdutoResponseDTO novoProduto = produtoService.criarProduto(produtoDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MensagemResponseDTO(
                            "Produto criado com sucesso!",
                            true,
                            novoProduto
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // GET - Listar todos os produtos
    @GetMapping
    public ResponseEntity<List<ProdutoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(produtoService.listarTodos());
    }

    // GET - Buscar produto por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            ProdutoResponseDTO produto = produtoService.buscarPorId(id);
            return ResponseEntity.ok(produto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // GET - Buscar produtos por categoria
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProdutoResponseDTO>> buscarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(produtoService.buscarPorCategoria(categoriaId));
    }

    // GET - Buscar produtos com estoque baixo
    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<ProdutoResponseDTO>> buscarEstoqueBaixo(
            @RequestParam(defaultValue = "10") Integer limite) {
        return ResponseEntity.ok(produtoService.buscarEstoqueBaixo(limite));
    }

    // GET - Buscar produtos com filtros
    @GetMapping("/buscar")
    public ResponseEntity<List<ProdutoResponseDTO>> buscarComFiltros(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) BigDecimal precoMin,
            @RequestParam(required = false) BigDecimal precoMax,
            @RequestParam(required = false) Boolean emEstoque,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) Boolean emPromocao) {

        return ResponseEntity.ok(produtoService.buscarComFiltros(
                nome, categoriaId, precoMin, precoMax, emEstoque, cidade, emPromocao));
    }

    // GET - Sugestões de produtos (autocomplete)
    @GetMapping("/sugestoes")
    public ResponseEntity<List<String>> listarSugestoes(@RequestParam String query) {
        if (query.length() < 3) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(produtoService.listarSugestoes(query));
    }

    // GET - Busca Inteligente via IA
    @GetMapping("/busca-ia")
    public Mono<List<ProdutoResponseDTO>> buscaIA(@RequestParam String query) {
        return produtoService.buscarPorIA(query);
    }

    // PUT - Atualizar produto completo
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarProduto(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoRequestDTO produtoDTO) {
        try {
            ProdutoResponseDTO produtoAtualizado = produtoService.atualizarProduto(id, produtoDTO);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Produto atualizado com sucesso!",
                    true,
                    produtoAtualizado
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // PATCH - Atualizar apenas o estoque
    @PatchMapping("/{id}/estoque")
    public ResponseEntity<?> atualizarEstoque(
            @PathVariable Long id,
            @RequestParam Integer quantidade) {
        try {
            ProdutoResponseDTO produto = produtoService.atualizarEstoque(id, quantidade);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Estoque atualizado com sucesso!",
                    true,
                    produto
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // DELETE - Deletar produto
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarProduto(@PathVariable Long id) {
        try {
            produtoService.deletarProduto(id);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Produto deletado com sucesso!",
                    true
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }
}