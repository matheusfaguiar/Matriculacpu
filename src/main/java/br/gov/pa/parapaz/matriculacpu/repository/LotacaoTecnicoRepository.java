package br.gov.pa.parapaz.matriculacpu.repository;

import br.gov.pa.parapaz.matriculacpu.entity.LotacaoTecnico;
import br.gov.pa.parapaz.matriculacpu.entity.Usuario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LotacaoTecnicoRepository extends JpaRepository<LotacaoTecnico, Integer> {

    @Query("SELECT lt FROM LotacaoTecnico lt JOIN FETCH lt.lotacao WHERE lt.usuario = :usuario")
    List<LotacaoTecnico> findByUsuario(@Param("usuario") Usuario tecnico);
}