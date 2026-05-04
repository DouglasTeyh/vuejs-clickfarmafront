package com.clickfarma.backend.repository;

import com.clickfarma.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNome(String nome);

    boolean existsByNome(String nome);

    @Query("SELECT DISTINCT c FROM Categoria c JOIN c.produtos p")
    List<Categoria> findCategoriasAtivas();
}