package br.gov.pa.parapaz.matriculacpu.repository;

import br.gov.pa.parapaz.matriculacpu.entity.Perfil;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepository extends JpaRepository<Perfil, Integer> {

    Optional<Perfil> findByDescricao(String string);}
