package com.clickfarma.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(length = 20)
    private String cpf;

    @Column(length = 20)
    private String telefone;

    @Column(length = 9)
    private String cep;

    @Column(length = 200)
    private String logradouro;

    @Column(length = 20)
    private String numero;

    @Column(length = 100)
    private String bairro;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(length = 200)
    private String complemento;

    @Column(length = 500)
    private String endereco; // Mantido para compatibilidade, mas os campos acima são preferíveis

    @Column(name = "telegram_id", length = 100)
    private String telegramId;

    @Column(name = "telegram_link_token", length = 120, unique = true)
    private String telegramLinkToken;

    @Column(name = "telegram_link_expires_at")
    private LocalDateTime telegramLinkExpiresAt;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @Column(length = 50)
    private String role; // "ADMIN", "PHARMACY", "COURIER", "CUSTOMER"

    @Column(length = 100)
    private String cidade;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Pedido> pedidos = new ArrayList<>();

    // Construtores
    public Usuario() {
        this.dataCadastro = LocalDateTime.now();
    }

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.dataCadastro = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getTelegramId() { return telegramId; }
    public void setTelegramId(String telegramId) { this.telegramId = telegramId; }

    public String getTelegramLinkToken() { return telegramLinkToken; }
    public void setTelegramLinkToken(String telegramLinkToken) { this.telegramLinkToken = telegramLinkToken; }

    public LocalDateTime getTelegramLinkExpiresAt() { return telegramLinkExpiresAt; }
    public void setTelegramLinkExpiresAt(LocalDateTime telegramLinkExpiresAt) { this.telegramLinkExpiresAt = telegramLinkExpiresAt; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public List<Pedido> getPedidos() { return pedidos; }
    public void setPedidos(List<Pedido> pedidos) { this.pedidos = pedidos; }
}
