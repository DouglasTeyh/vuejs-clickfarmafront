package com.clickfarma.backend.dto;

/**
 * DTO para requisição de processamento de receita médica
 * Recebe a imagem em Base64 do frontend
 */
public class ReceitaRequestDTO {
    private String imagemBase64;  // A imagem codificada em Base64 (sem o prefixo data:image)
    private String nomeArquivo;   // Nome do arquivo (ex: receita.jpg)

    // Construtores
    public ReceitaRequestDTO() {
    }

    public ReceitaRequestDTO(String imagemBase64, String nomeArquivo) {
        this.imagemBase64 = imagemBase64;
        this.nomeArquivo = nomeArquivo;
    }

    // Getters e Setters
    public String getImagemBase64() {
        return imagemBase64;
    }

    public void setImagemBase64(String imagemBase64) {
        this.imagemBase64 = imagemBase64;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    @Override
    public String toString() {
        return "ReceitaRequestDTO{" +
                "nomeArquivo='" + nomeArquivo + '\'' +
                ", imagemBase64Length=" + (imagemBase64 != null ? imagemBase64.length() : 0) +
                '}';
    }
}

