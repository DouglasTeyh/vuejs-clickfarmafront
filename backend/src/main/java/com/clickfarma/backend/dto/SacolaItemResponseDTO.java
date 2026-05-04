package com.clickfarma.backend.dto;

import com.clickfarma.backend.model.SacolaItem;
import java.time.LocalDateTime;

public class SacolaItemResponseDTO {
    private Long id;
    private ProdutoResponseDTO produto;
    private Integer quantidade;
    private LocalDateTime dataAdicao;

    public SacolaItemResponseDTO(SacolaItem item) {
        this.id = item.getId();
        this.produto = new ProdutoResponseDTO(item.getProduto());
        this.quantidade = item.getQuantidade();
        this.dataAdicao = item.getDataAdicao();
    }

    // Getters
    public Long getId() { return id; }
    public ProdutoResponseDTO getProduto() { return produto; }
    public Integer getQuantidade() { return quantidade; }
    public LocalDateTime getDataAdicao() { return dataAdicao; }
}
