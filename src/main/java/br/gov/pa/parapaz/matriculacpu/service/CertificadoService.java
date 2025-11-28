package br.gov.pa.parapaz.matriculacpu.service;

import br.gov.pa.parapaz.matriculacpu.entity.*;
import br.gov.pa.parapaz.matriculacpu.repository.MatriculaRepository;
import br.gov.pa.parapaz.matriculacpu.repository.LotacaoTecnicoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.*;

@Service
public class CertificadoService {

    @Value("${app.certificados.diretorio:certificados-gerados}")
    private String certificadosDiretorio;

    private final MatriculaRepository matriculaRepository;
    private final LotacaoTecnicoRepository lotacaoTecnicoRepository;
    private final PdfCertificadoGenerator pdfGenerator;

    public CertificadoService(MatriculaRepository matriculaRepository, 
                            LotacaoTecnicoRepository lotacaoTecnicoRepository,
                            PdfCertificadoGenerator pdfGenerator) {
        this.matriculaRepository = matriculaRepository;
        this.lotacaoTecnicoRepository = lotacaoTecnicoRepository;
        this.pdfGenerator = pdfGenerator;
    }

    /**
     * Itera sobre todas as matrículas de um curso e gera certificados.
     * Retorna a lista de nomes dos alunos cujo certificado foi gerado.
     */
    @Transactional
    public List<Map<String, String>> gerarCertificadosDoCurso(Curso curso) throws Exception {
        List<Matricula> matriculas = matriculaRepository.findByCursoId((long) curso.getId());
        List<Map<String, String>> resultado = new ArrayList<>();

        for (Matricula m : matriculas) {
            // Só gera certificado para alunos aprovados
            if (m.getAlunoAprovado() == null || !m.getAlunoAprovado()) {
                continue;
            }

            // Define o nome do arquivo organizado por ID do aluno
            String nomeArquivo = m.getUsuario().getNome().replaceAll("\\s+", "_")
                    + "_-_" + curso.getNome().replaceAll("\\s+", "_") + "_certificado.pdf";

            // Gera o caminho organizado por ID do aluno
            String caminhoCertificado = gerarCaminhoCertificado(m.getUsuario().getId(), nomeArquivo);

            try {
                // Gera o certificado e salva no disco
                pdfGenerator.gerarCertificado(
                        (curso.getLotacao().getTipoLotacao().getId() == 1) ? "usina" : "unidade",
                        m.getUsuario().getNome(),
                        curso.getNome(),
                        curso.getLotacao().getNome(),
                        curso.getCargaHoraria().toString(),
                        curso.getDataInicio(),
                        curso.getDataFim(),
                        curso.getLotacao().getCidade(),
                        curso.getMatrizCurricular(),
                        caminhoCertificado, // Usa o caminho organizado
                        false // não é preview
                );

                // Atualiza a matrícula com a URL do certificado
                m.setUrlCertificado(caminhoCertificado);
                matriculaRepository.save(m);

                Map<String, String> map = new HashMap<>();
                map.put("nome", m.getUsuario().getNome());
                map.put("urlCertificado", m.getUrlCertificado());
                map.put("status", "SUCESSO");
                resultado.add(map);

            } catch (Exception e) {
                Map<String, String> map = new HashMap<>();
                map.put("nome", m.getUsuario().getNome());
                map.put("status", "ERRO");
                map.put("erro", e.getMessage());
                resultado.add(map);
            }
        }

        return resultado;
    }

    /**
     * Gera preview do certificado usando placeholder de nome do aluno.
     */
    public byte[] gerarPreviewCertificado(Curso curso) throws Exception {
        return gerarCertificado("NOME DO ALUNO", curso, true);
    }

    /**
     * Gera um certificado a partir do nome do aluno e informações do curso.
     * @param preview se true, não salva em disco, retorna bytes para preview
     */
    public byte[] gerarCertificado(String nomeAluno, Curso curso, boolean preview) throws Exception {
        String modelo = (curso.getLotacao().getTipoLotacao().getId() == 1)
                ? "usina"
                : "unidade";

        String nomeArquivo = nomeAluno.replaceAll("\\s+", "_") + "_-_" + 
                           curso.getNome().replaceAll("\\s+", "_") + "_certificado.pdf";

        if (preview) {
            // Gera apenas em memória para preview
            return pdfGenerator.gerarCertificado(
                    modelo,
                    nomeAluno,
                    curso.getNome(),
                    curso.getLotacao().getNome(),
                    curso.getCargaHoraria().toString(),
                    curso.getDataInicio(),
                    curso.getDataFim(),
                    curso.getLotacao().getCidade(),
                    curso.getMatrizCurricular(),
                    nomeArquivo,
                    true // preview
            );
        } else {
            // Gera e salva em disco (para teste individual)
            String caminhoCertificado = gerarCaminhoCertificado(0, nomeArquivo); // ID 0 para previews
            
            pdfGenerator.gerarCertificado(
                    modelo,
                    nomeAluno,
                    curso.getNome(),
                    curso.getLotacao().getNome(),
                    curso.getCargaHoraria().toString(),
                    curso.getDataInicio(),
                    curso.getDataFim(),
                    curso.getLotacao().getCidade(),
                    curso.getMatrizCurricular(),
                    caminhoCertificado,
                    false // não é preview
            );
            
            return Files.readAllBytes(Paths.get(caminhoCertificado));
        }
    }

    /**
     * Gera certificado para uma matrícula específica
     */
    @Transactional
    public String gerarCertificadoParaMatricula(Matricula matricula) throws Exception {
        Curso curso = matricula.getCurso();
        Usuario aluno = matricula.getUsuario();

        // Verifica se o aluno está aprovado
        if (matricula.getAlunoAprovado() == null || !matricula.getAlunoAprovado()) {
            throw new IllegalArgumentException("Aluno não está aprovado no curso");
        }

        // Define o nome do arquivo
        String nomeArquivo = aluno.getNome().replaceAll("\\s+", "_")
                + "_-_" + curso.getNome().replaceAll("\\s+", "_") + "_certificado.pdf";

        // Gera o caminho organizado por ID do aluno
        String caminhoCertificado = gerarCaminhoCertificado(aluno.getId(), nomeArquivo);

        // Gera o certificado
        pdfGenerator.gerarCertificado(
                (curso.getLotacao().getTipoLotacao().getId() == 1) ? "usina" : "unidade",
                aluno.getNome(),
                curso.getNome(),
                curso.getLotacao().getNome(),
                curso.getCargaHoraria().toString(),
                curso.getDataInicio(),
                curso.getDataFim(),
                curso.getLotacao().getCidade(),
                curso.getMatrizCurricular(),
                caminhoCertificado,
                false // não é preview
        );

        // Atualiza a matrícula com a URL do certificado
        matricula.setUrlCertificado(caminhoCertificado);
        matriculaRepository.save(matricula);

        return caminhoCertificado;
    }

    /**
     * Obtém o certificado de uma matrícula com controle de acesso
     */
    public Resource obterCertificado(Integer matriculaId, Usuario usuarioRequisitante) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada."));

        // Verifica permissões
        if (!temPermissaoAcessoCertificado(usuarioRequisitante, matricula)) {
            throw new AccessDeniedException("Você não tem permissão para acessar este certificado.");
        }

        // Verifica se existe certificado
        if (matricula.getUrlCertificado() == null || matricula.getUrlCertificado().isEmpty()) {
            throw new IllegalArgumentException("Certificado não encontrado para esta matrícula.");
        }

        try {
            Path filePath = Paths.get(matricula.getUrlCertificado()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalArgumentException("Certificado não encontrado no sistema de arquivos.");
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Erro ao acessar o certificado: " + e.getMessage());
        }
    }

    /**
     * Verifica se o usuário tem permissão para acessar o certificado
     */
    public boolean temPermissaoAcessoCertificado(Usuario usuarioRequisitante, Matricula matricula) {
        Integer perfilId = usuarioRequisitante.getPerfil().getId();
        
        // Admin (perfil.id = 1) tem acesso total
        if (perfilId == 1) {
            return true;
        }

        // O próprio aluno (perfil.id = 3) tem acesso
        if (perfilId == 3 && 
            matricula.getUsuario().getId().equals(usuarioRequisitante.getId())) {
            return true;
        }

        // Técnico (perfil.id = 2) da mesma lotação do curso
        if (perfilId == 2) {
            return isTecnicoDaMesmaLotacao(usuarioRequisitante, matricula.getCurso().getLotacao());
        }

        return false;
    }

    /**
     * Verifica se o técnico está lotado na mesma lotação do curso
     */
    private boolean isTecnicoDaMesmaLotacao(Usuario tecnico, Lotacao lotacaoCurso) {
        // Verifica se o técnico tem uma lotação técnica na mesma lotação do curso
        List<LotacaoTecnico> lotacoesTecnico = lotacaoTecnicoRepository.findByUsuario(tecnico);
        
        return lotacoesTecnico.stream()
                .anyMatch(lotacaoTec -> lotacaoTec.getLotacao().getId().equals(lotacaoCurso.getId()));
    }

    /**
     * Gera o caminho do certificado organizado por ID do aluno
     */
    public String gerarCaminhoCertificado(Integer alunoId, String nomeArquivo) {
        String pastaAluno = "aluno_" + alunoId;
        Path diretorioAluno = Paths.get(certificadosDiretorio, pastaAluno);
        
        try {
            Files.createDirectories(diretorioAluno);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar diretório do aluno: " + e.getMessage(), e);
        }
        
        return diretorioAluno.resolve(nomeArquivo).toString();
    }

    /**
     * Lista todos os certificados que um usuário pode acessar
     */
    public List<Matricula> listarCertificadosAcessiveis(Usuario usuario) {
        List<Matricula> todasMatriculas = matriculaRepository.findAll();
        List<Matricula> acessiveis = new ArrayList<>();
        
        for (Matricula matricula : todasMatriculas) {
            if (temPermissaoAcessoCertificado(usuario, matricula) && 
                matricula.getUrlCertificado() != null && 
                !matricula.getUrlCertificado().isEmpty()) {
                acessiveis.add(matricula);
            }
        }
        
        return acessiveis;
    }

    /**
     * Verifica se um certificado existe para uma matrícula
     */
    public boolean certificadoExiste(Integer matriculaId) {
        Matricula matricula = matriculaRepository.findById(matriculaId).orElse(null);
        if (matricula == null || matricula.getUrlCertificado() == null) {
            return false;
        }
        
        try {
            Path filePath = Paths.get(matricula.getUrlCertificado());
            return Files.exists(filePath) && Files.isReadable(filePath);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Remove certificado de uma matrícula
     */
    @Transactional
    public boolean removerCertificado(Integer matriculaId, Usuario usuario) {
        Matricula matricula = matriculaRepository.findById(matriculaId).orElse(null);
        if (matricula == null || matricula.getUrlCertificado() == null) {
            return false;
        }

        // Verifica permissão
        if (!temPermissaoAcessoCertificado(usuario, matricula) && usuario.getPerfil().getId() != 1) {
            throw new AccessDeniedException("Sem permissão para remover este certificado");
        }

        try {
            // Remove arquivo físico
            Path filePath = Paths.get(matricula.getUrlCertificado());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            // Remove referência no banco
            matricula.setUrlCertificado(null);
            matriculaRepository.save(matricula);
            return true;

        } catch (IOException e) {
            throw new RuntimeException("Erro ao remover arquivo do certificado: " + e.getMessage(), e);
        }
    }
}