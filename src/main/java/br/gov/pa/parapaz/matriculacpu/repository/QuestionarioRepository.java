package br.gov.pa.parapaz.matriculacpu.repository;

import br.gov.pa.parapaz.matriculacpu.entity.Matricula;
import br.gov.pa.parapaz.matriculacpu.entity.Questionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionarioRepository extends JpaRepository<Questionario, Integer> {

    @Query("SELECT q FROM Questionario q JOIN FETCH q.matricula WHERE q.matricula = :matricula")
    Questionario findByMatricula(@Param("matricula") Matricula matriculaExistente);
}