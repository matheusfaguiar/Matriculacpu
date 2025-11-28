// br.gov.pa.parapaz.matriculacpu.repository.CursoRepository.java
package br.gov.pa.parapaz.matriculacpu.repository;

import br.gov.pa.parapaz.matriculacpu.entity.Lotacao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LotacaoRepository extends JpaRepository<Lotacao, Integer> {
    
    List<Lotacao> findByIdIn(List<Integer> ids);
}
