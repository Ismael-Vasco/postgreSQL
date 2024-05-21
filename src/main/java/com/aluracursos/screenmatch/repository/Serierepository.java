package com.aluracursos.screenmatch.repository;

import com.aluracursos.screenmatch.model.Categoria;
import com.aluracursos.screenmatch.model.Episodio;
import com.aluracursos.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface Serierepository extends JpaRepository<Serie,Long> {

    // buscar series por el nombre
    Optional<Serie> findByTituloContainsIgnoreCase(String nombreSerie);

    // top 5 serie mejor evaluadas
    List<Serie> findTop5ByOrderByEvaluacionDesc();

    // buscar serie por categoria
    List<Serie> findByGenero(Categoria categoria);

    // bucar por tiempos y evaluación
    //List<Serie> findByTotalTemporadasLessThanEqualAndEvaluacionGreaterThanEqual(int totalTemporadas, Double evaluacion);

    @Query("SELECT s FROM Serie s WHERE s.totalTemporadas <= :totalTemporadas AND s.evaluacion >= :evaluacion")
    List<Serie> seriesPorTemporadaYEvaluacion(int totalTemporadas, Double evaluacion);

    // episodios por nombre
    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE e.titulo ILIKE %:nombreEpisodio%")
    List<Episodio> episodiosporNombre(String nombreEpisodio);


    // tops 5 mejores episodios calificados
    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s = :serie ORDER BY e.evaluacion  DESC LIMIT 5")
    List<Episodio> top5Episodios (Serie serie);
}
