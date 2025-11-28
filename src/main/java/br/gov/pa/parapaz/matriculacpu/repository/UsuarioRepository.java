package br.gov.pa.parapaz.matriculacpu.repository;

import br.gov.pa.parapaz.matriculacpu.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    @Query("SELECT u FROM Usuario u JOIN FETCH u.perfil WHERE u.firebaseUid = :firebaseUid OR u.email = :email")
    Optional<Usuario> findByFirebaseUidOrEmail(
        @Param("firebaseUid") String firebaseUid, 
        @Param("email") String email);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.perfil WHERE u.email = :email")
    Optional<Usuario> findByEmail(@Param("email") String email);
}