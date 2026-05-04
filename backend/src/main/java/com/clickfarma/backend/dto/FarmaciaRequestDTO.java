package com.clickfarma.backend.dto;

import jakarta.validation.constraints.NotBlank;
public class FarmaciaRequestDTO {
    @NotBlank
    private String nome;
    @NotBlank
    private String cnpj;
    private String endereco;
    private String cidade;
    private String telefone;
    private String email;
    private String fotoUrl;
    @NotBlank
    private String senha;
    private String chavePix;
    private String tipoChavePix;

    // Getters and Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getChavePix() { return chavePix; }
    public void setChavePix(String chavePix) { this.chavePix = chavePix; }

    public String getTipoChavePix() { return tipoChavePix; }
    public void setTipoChavePix(String tipoChavePix) { this.tipoChavePix = tipoChavePix; }
}
