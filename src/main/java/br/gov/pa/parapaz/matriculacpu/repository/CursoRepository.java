package br.gov.pa.parapaz.matriculacpu.repository;

import br.gov.pa.parapaz.matriculacpu.entity.Curso;
import br.gov.pa.parapaz.matriculacpu.entity.Lotacao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CursoRepository extends JpaRepository<Curso, Integer> {

    @Query("SELECT c FROM Curso c JOIN FETCH c.lotacao WHERE c.lotacao.id = :lotacaoId")
    List<Curso> findByLotacaoId(@Param("lotacaoId") Integer lotacaoId);

    @Query("SELECT c FROM Curso c JOIN FETCH c.lotacao WHERE c.dataInicio >= :dataInicio AND c.vagas > :vagas")
    List<Curso> findByDataInicioGreaterThanEqualAndVagasGreaterThan(
        @Param("dataInicio") LocalDate dataInicio, 
        @Param("vagas") int vagas);

    @Query("SELECT c FROM Curso c JOIN FETCH c.lotacao WHERE c.lotacao.id IN :lotacaoIds ORDER BY c.nome")
    List<Curso> findByLotacaoIds(@Param("lotacaoIds") List<Integer> lotacaoIds);

    @Query("SELECT c FROM Curso c JOIN FETCH c.lotacao WHERE c.lotacao = :lotacao")
    List<Curso> findByLotacao(@Param("lotacao") Lotacao lotacaoTecnico, Sort sort);
}