package com.clickfarma.backend.dto;

public class AlternativaSugeridaDTO {
    private Long produtoId;
    private String nome;
    private Double preco;
    private Integer estoque;
    private String motivo;

    public AlternativaSugeridaDTO() {}

    public AlternativaSugeridaDTO(Long produtoId, String nome, Double preco, Integer estoque, String motivo) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
        this.motivo = motivo;
    }

    // Getters e Setters
    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }

    public Integer getEstoque() { return estoque; }
    public void setEstoque(Integer estoque) { this.estoque = estoque; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
