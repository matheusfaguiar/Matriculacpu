package br.gov.pa.parapaz.matriculacpu.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.pa.parapaz.matriculacpu.entity.Perfil;
import br.gov.pa.parapaz.matriculacpu.entity.Usuario;
import br.gov.pa.parapaz.matriculacpu.repository.PerfilRepository;
import br.gov.pa.parapaz.matriculacpu.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PerfilRepository perfilRepository) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
    }

    @Transactional
    public Usuario buscarOuCriar(String uid, String email, String nome) {
        return usuarioRepository.findByFirebaseUidOrEmail(uid, email)
                .orElseGet(() -> {
                    Usuario usuario = new Usuario();
                    usuario.setFirebaseUid(uid);
                    usuario.setEmail(email);
                    usuario.setNome(nome);
                    usuario.setProvider("GOOGLE");
                    usuario.setDataCriacao(LocalDateTime.now());

                    Perfil perfil = perfilRepository.findByDescricao("Aluno")
                            .orElseThrow(() -> new RuntimeException("Perfil 'Aluno' não encontrado"));
                    usuario.setPerfil(perfil);

                    System.out.println("Criando novo usuário temporário: " + email);

                    return usuarioRepository.save(usuario);
                });
    }

    // novo método para salvar/atualizar usuário
    public Usuario salvar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // opcional: método específico para só atualizar o último login
    public void atualizarUltimoLogin(Integer usuarioId) {
        usuarioRepository.findById(usuarioId).ifPresent(u -> {
            u.setUltimoLogin(LocalDateTime.now());
            usuarioRepository.save(u);
        });
    }
    }