package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByCategoriaId(Long categoriaId);

    List<Produto> findByNomeContainingIgnoreCase(String nome);

    List<Produto> findByPrecoBetween(BigDecimal precoMin, BigDecimal precoMax);

    List<Produto> findByEstoqueLessThan(Integer limite);

    @Query("SELECT p FROM Produto p LEFT JOIN p.farmacia f LEFT JOIN p.categoria c WHERE " +
            "(:nome IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', CAST(:nome AS text), '%')) OR " +
            " LOWER(p.descricao) LIKE LOWER(CONCAT('%', CAST(:nome AS text), '%')) OR " +
            " LOWER(p.principioAtivo) LIKE LOWER(CONCAT('%', CAST(:nome AS text), '%'))) AND " +
            "(:categoriaId IS NULL OR c.id = :categoriaId) AND " +
            "(:precoMin IS NULL OR p.preco >= :precoMin) AND " +
            "(:precoMax IS NULL OR p.preco <= :precoMax) AND " +
            "(:cidade IS NULL OR LOWER(f.cidade) = LOWER(CAST(:cidade AS text))) AND " +
            "(:emPromocao IS NULL OR p.emPromocao = :emPromocao)")
    List<Produto> buscarProdutosFiltrados(
            @Param("nome") String nome,
            @Param("categoriaId") Long categoriaId,
            @Param("precoMin") BigDecimal precoMin,
            @Param("precoMax") BigDecimal precoMax,
            @Param("cidade") String cidade,
            @Param("emPromocao") Boolean emPromocao);

    List<Produto> findByFarmaciaCidadeIgnoreCase(String cidade);

    // ========== MÉTODOS ADICIONAIS ==========

    List<Produto> findByNomeStartingWithIgnoreCase(String nome);

    List<Produto> findByPrincipioAtivoStartingWithIgnoreCase(String principioAtivo);

    @Query("SELECT p FROM Produto p WHERE " +
            "LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<Produto> buscarPorNomeOuDescricao(@Param("termo") String termo);

    Optional<Produto> findByNomeIgnoreCase(String nome);

    List<Produto> findByFarmaciaId(Long farmaciaId);
}