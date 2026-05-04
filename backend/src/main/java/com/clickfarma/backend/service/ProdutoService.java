package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.ProdutoRequestDTO;
import com.clickfarma.backend.dto.ProdutoResponseDTO;
import com.clickfarma.backend.model.Categoria;
import com.clickfarma.backend.model.Farmacia;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.repository.CategoriaRepository;
import com.clickfarma.backend.repository.FarmaciaRepository;
import com.clickfarma.backend.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private FarmaciaRepository farmaciaRepository;

    @Autowired
    private AiRouterService aiRouterService;

    @Autowired
    private FileStorageService fileStorageService;

    // Criar produto
    public ProdutoResponseDTO criarProduto(ProdutoRequestDTO produtoDTO) {
        Produto produto = new Produto();
        aplicarCampos(produto, produtoDTO);
        Produto produtoSalvo = produtoRepository.save(produto);
        return new ProdutoResponseDTO(produtoSalvo);
    }

    // Atualizar produto
    public ProdutoResponseDTO atualizarProduto(Long id, ProdutoRequestDTO produtoDTO) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
        aplicarCampos(produto, produtoDTO);
        produto.setDataAtualizacao(LocalDateTime.now());
        return new ProdutoResponseDTO(produtoRepository.save(produto));
    }

    private void aplicarCampos(Produto produto, ProdutoRequestDTO dto) {
        if (dto.getNome() != null) produto.setNome(dto.getNome());
        if (dto.getDescricaoBreve() != null) produto.setDescricaoBreve(dto.getDescricaoBreve());
        if (dto.getDescricao() != null) produto.setDescricao(dto.getDescricao());
        if (dto.getPreco() != null) produto.setPreco(dto.getPreco());
        if (dto.getEstoque() != null) produto.setEstoque(dto.getEstoque());
        if (dto.getImageUrl() != null) produto.setImageUrl(dto.getImageUrl());
        if (dto.getPrincipioAtivo() != null) produto.setPrincipioAtivo(dto.getPrincipioAtivo());
        if (dto.getDosagem() != null) produto.setDosagem(dto.getDosagem());
        if (dto.getLaboratorio() != null) produto.setLaboratorio(dto.getLaboratorio());
        if (dto.getNecessitaReceita() != null) produto.setNecessitaReceita(dto.getNecessitaReceita());
        if (dto.getCategoriaId() != null) {
            categoriaRepository.findById(dto.getCategoriaId()).ifPresent(produto::setCategoria);
        }
        if (dto.getFarmaciaId() != null) {
            farmaciaRepository.findById(dto.getFarmaciaId()).ifPresent(produto::setFarmacia);
        }
        if (dto.getEmPromocao() != null) produto.setEmPromocao(dto.getEmPromocao());
        if (dto.getDescontoPercentual() != null) {
            BigDecimal desconto = dto.getDescontoPercentual();
            if (desconto.compareTo(BigDecimal.ZERO) >= 0 && desconto.compareTo(new BigDecimal("20")) <= 0) {
                produto.setDescontoPercentual(desconto);
            } else {
                throw new RuntimeException("O desconto deve ser entre 0% e 20%");
            }
        }
    }

    // Listar todos
    public List<ProdutoResponseDTO> listarTodos() {
        return produtoRepository.findAll()
                .stream()
                .map(ProdutoResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Buscar por ID
    public ProdutoResponseDTO buscarPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
        return new ProdutoResponseDTO(produto);
    }

    // Buscar por categoria
    public List<ProdutoResponseDTO> buscarPorCategoria(Long categoriaId) {
        return produtoRepository.findByCategoriaId(categoriaId)
                .stream()
                .map(ProdutoResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Buscar com filtros (atualizado com emEstoque)
    public List<ProdutoResponseDTO> buscarComFiltros(
            String nome,
            Long categoriaId,
            BigDecimal precoMin,
            BigDecimal precoMax,
            Boolean emEstoque,
            String cidade,
            Boolean emPromocao) {

        List<Produto> produtos = produtoRepository.buscarProdutosFiltrados(
                nome, categoriaId, precoMin, precoMax, cidade, emPromocao);

        // Filtrar por disponibilidade em estoque se solicitado
        if (emEstoque != null && emEstoque) {
            produtos = produtos.stream()
                    .filter(p -> p.getEstoque() > 0)
                    .collect(Collectors.toList());
        }

        return produtos.stream()
                .map(ProdutoResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Buscar produtos com estoque baixo
    public List<ProdutoResponseDTO> buscarEstoqueBaixo(Integer limite) {
        return produtoRepository.findByEstoqueLessThan(limite)
                .stream()
                .map(ProdutoResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Atualizar estoque
    public ProdutoResponseDTO atualizarEstoque(Long id, Integer quantidade) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        int novoEstoque = produto.getEstoque() + quantidade;
        if (novoEstoque < 0) {
            throw new RuntimeException("Estoque insuficiente. Disponível: " + produto.getEstoque());
        }

        produto.setEstoque(novoEstoque);
        produto.setDataAtualizacao(LocalDateTime.now());

        Produto produtoAtualizado = produtoRepository.save(produto);
        return new ProdutoResponseDTO(produtoAtualizado);
    }

    // Deletar produto
    public void deletarProduto(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
        
        // Deletar a imagem do disco se existir
        if (produto.getImageUrl() != null) {
            fileStorageService.deletarArquivo(produto.getImageUrl());
        }
        
        produtoRepository.delete(produto);
    }

    public Mono<List<ProdutoResponseDTO>> buscarPorIA(String query) {
        return aiRouterService.interpretSearchQuery(query)
                .map(keywords -> {
                    String[] words = keywords.split(",");
                    return produtoRepository.findAll().stream()
                            .filter(p -> {
                                for (String word : words) {
                                    String w = word.trim().toLowerCase();
                                    if (p.getNome().toLowerCase().contains(w) || 
                                        (p.getDescricao() != null && p.getDescricao().toLowerCase().contains(w)) ||
                                        (p.getPrincipioAtivo() != null && p.getPrincipioAtivo().toLowerCase().contains(w))) {
                                        return true;
                                    }
                                }
                                return false;
                            })
                            .map(ProdutoResponseDTO::new)
                            .collect(Collectors.toList());
                });
    }

    public List<String> listarSugestoes(String query) {
        List<String> sugestoesNome = produtoRepository.findByNomeStartingWithIgnoreCase(query)
                .stream().map(Produto::getNome).collect(Collectors.toList());
        List<String> sugestoesAtivo = produtoRepository.findByPrincipioAtivoStartingWithIgnoreCase(query)
                .stream().map(Produto::getPrincipioAtivo).filter(java.util.Objects::nonNull).collect(Collectors.toList());
        
        sugestoesNome.addAll(sugestoesAtivo);
        return sugestoesNome.stream().distinct().limit(10).collect(Collectors.toList());
    }
}
