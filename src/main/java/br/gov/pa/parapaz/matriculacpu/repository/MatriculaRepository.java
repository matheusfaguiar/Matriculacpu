package br.gov.pa.parapaz.matriculacpu.repository;

import br.gov.pa.parapaz.matriculacpu.entity.Curso;
import br.gov.pa.parapaz.matriculacpu.entity.Matricula;
import br.gov.pa.parapaz.matriculacpu.entity.Usuario;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatriculaRepository extends JpaRepository<Matricula, Integer> {

    @Query("SELECT m FROM Matricula m JOIN FETCH m.usuario JOIN FETCH m.curso WHERE m.usuario = :usuario AND m.curso = :curso")
    Matricula findByUsuarioAndCurso(
        @Param("usuario") Usuario usuarioLogado, 
        @Param("curso") Curso curso);

    @Query("SELECT m FROM Matricula m JOIN FETCH m.usuario WHERE m.curso.id = :cursoId")
    List<Matricula> findByCursoId(@Param("cursoId") Long cursoId);

    @Query("SELECT m FROM Matricula m JOIN FETCH m.curso WHERE m.usuario.id = :usuarioId")
    List<Matricula> findByUsuarioId(@Param("usuarioId") int usuarioId, Sort sort);

    @Query("SELECT m FROM Matricula m JOIN FETCH m.curso WHERE m.usuario = :usuario AND m.curso.dataInicio >= :dataInicio")
    List<Matricula> findByUsuarioAndCurso_DataInicioGreaterThanEqual(
        @Param("usuario") Usuario usuario, 
        @Param("dataInicio") LocalDate dataInicio);

    @Query("SELECT m FROM Matricula m JOIN FETCH m.curso WHERE m.usuario = :usuario")
    List<Matricula> findByUsuario(@Param("usuario") Usuario usuarioLogado);

    @Query("SELECT m FROM Matricula m JOIN FETCH m.usuario WHERE m.curso.id = :cursoId ORDER BY m.efetivada DESC, m.usuario.nome ASC")
    List<Matricula> findByCursoIdOrderByEfetivadaDescUsuarioNomeAsc(@Param("cursoId") Integer cursoId);
}