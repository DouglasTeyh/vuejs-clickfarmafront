package com.clickfarma.backend.service;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root = Paths.get("uploads");

    @PostConstruct
    public void init() {
        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível inicializar a pasta de uploads!");
        }
    }

    public String salvarImagemProduto(MultipartFile arquivo, String nomeFarmacia) {
        try {
            // Normalizar nome da farmácia para pasta
            String pastaFarmacia = nomeFarmacia.toLowerCase()
                    .replaceAll("[^a-z0-9]", "_");
            
            Path caminhoFarmacia = this.root.resolve("products").resolve(pastaFarmacia);
            
            if (!Files.exists(caminhoFarmacia)) {
                Files.createDirectories(caminhoFarmacia);
            }

            // Gerar nome único para o arquivo
            String extensao = getExtensao(arquivo.getOriginalFilename());
            String nomeArquivo = UUID.randomUUID().toString() + extensao;

            Files.copy(arquivo.getInputStream(), caminhoFarmacia.resolve(nomeArquivo), StandardCopyOption.REPLACE_EXISTING);

            // Retornar o caminho relativo para salvar no banco
            return "/uploads/products/" + pastaFarmacia + "/" + nomeArquivo;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar imagem: " + e.getMessage());
        }
    }

    public void deletarArquivo(String caminhoRelativo) {
        if (caminhoRelativo == null || caminhoRelativo.isEmpty()) return;
        
        try {
            // Remove o prefixo "/uploads/" se existir para obter o path relativo ao root
            String pathLimpo = caminhoRelativo.startsWith("/uploads/") 
                    ? caminhoRelativo.substring(9) 
                    : caminhoRelativo;
            
            Path arquivo = this.root.resolve(pathLimpo);
            Files.deleteIfExists(arquivo);
            System.out.println("✅ Arquivo deletado com sucesso: " + arquivo.toString());
        } catch (IOException e) {
            System.err.println("⚠️ Erro ao deletar arquivo: " + e.getMessage());
        }
    }

    private String getExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) return ".jpg";
        return nomeArquivo.substring(nomeArquivo.lastIndexOf("."));
    }
}
