package br.gov.pa.parapaz.matriculacpu.controller;

import br.gov.pa.parapaz.matriculacpu.entity.Curso;
import br.gov.pa.parapaz.matriculacpu.entity.Matricula;
import br.gov.pa.parapaz.matriculacpu.entity.Perfil;
import br.gov.pa.parapaz.matriculacpu.entity.Questionario;
import br.gov.pa.parapaz.matriculacpu.entity.Usuario;
import br.gov.pa.parapaz.matriculacpu.repository.CursoRepository;
import br.gov.pa.parapaz.matriculacpu.repository.MatriculaRepository;
import br.gov.pa.parapaz.matriculacpu.repository.PerfilRepository;
import br.gov.pa.parapaz.matriculacpu.repository.QuestionarioRepository;
import br.gov.pa.parapaz.matriculacpu.service.CertificadoService;
import br.gov.pa.parapaz.matriculacpu.service.EmailService;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/matricula")
public class MatriculaController extends BaseController {

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private QuestionarioRepository questionarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private CertificadoService certificadoService;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public String listarMatriculas(Model model, Authentication authentication) {
        if (!prepareLayout(model, authentication, "Matrículas", "matricula/list")) {
            return "redirect:/";
        }

        List<Matricula> matriculas = matriculaRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("matriculas", matriculas);

        return "layout";
    }

    // =======================================
    // NOVA MATRÍCULA + QUESTIONÁRIO
    // =======================================
    @GetMapping("/nova")
    @Transactional
    public String criarMatriculaEExibirQuestionario(@RequestParam Integer cursoId,
                                                    HttpServletRequest request,
                                                    Model model,
                                                    Authentication authentication) {
        if (!prepareLayout(model, authentication, "Questionário de Matrícula", "matricula/questionario")) {
            return "redirect:/";
        }

        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/";
        }

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso inválido: " + cursoId));

        Matricula matricula = matriculaRepository.findByUsuarioAndCurso(usuarioLogado, curso);
        Questionario questionario;

        if (matricula != null) {
            Questionario existente = questionarioRepository.findByMatricula(matricula);
            questionario = existente != null ? existente : new Questionario();
            
            // CORREÇÃO: Definir o matriculaId no questionário
            questionario.setMatriculaId(matricula.getId());

            if (matricula.isEfetivada()) {
                // Já concluída → vai para confirmação
                return "redirect:/matricula/confirmacao?matriculaId=" + matricula.getId();
            }
        } else {
            // Cria nova matrícula
            matricula = new Matricula();
            matricula.setCurso(curso);
            matricula.setUsuario(usuarioLogado);
            matricula.setEfetivada(false);
            matricula = matriculaRepository.save(matricula);

            questionario = new Questionario();
            // CORREÇÃO: Definir o matriculaId no questionário
            questionario.setMatriculaId(matricula.getId());
        }

        // ATRIBUTOS ESSENCIAIS
        model.addAttribute("curso", curso);
        model.addAttribute("matricula", matricula);
        model.addAttribute("matriculaId", matricula.getId());
        model.addAttribute("usuario", usuarioLogado);
        model.addAttribute("questionario", questionario); // Agora com matriculaId definido

        List<Perfil> perfis = perfilRepository.findAll();
        model.addAttribute("perfis", perfis);

        return "layout";
    }

    // =======================================
    // SALVAR QUESTIONÁRIO (VERSÃO CORRIGIDA - PRESERVA RESPOSTAS)
    // =======================================
    @PostMapping("/concluir")
    @Transactional
    public String salvarQuestionario(@Valid @ModelAttribute("questionario") Questionario questionario,
                                    BindingResult bindingResult,
                                    HttpServletRequest request,
                                    Model model, // ADICIONADO: Model para reexibir com dados
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/";
        }

        // Validações básicas
        if (questionario.getMatriculaId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID da matrícula não informado.");
            return "redirect:/";
        }
        if (questionario.getCep() != null && !questionario.getCep().isEmpty()) {
            String cepLimpo = questionario.getCep().replaceAll("[^0-9]", "");
            questionario.setCep(cepLimpo);
        }

        Matricula matricula = matriculaRepository.findById(questionario.getMatriculaId())
                .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada."));

        // Verifica permissão
        if (usuarioLogado.getPerfil().getId() == 3 && 
            !matricula.getUsuario().getId().equals(usuarioLogado.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Você não tem permissão para esta ação.");
            return "redirect:/matricula/minhas-matriculas";
        }

        // Validação do formulário - SE HOUVER ERROS, REEXIBE O FORMULÁRIO COM OS DADOS
        if (bindingResult.hasErrors()) {
            System.out.println("Erros de validação: " + bindingResult.getAllErrors());
            
            // PREPARA O LAYOUT PARA REEXIBIR O FORMULÁRIO COM OS DADOS
            if (!prepareLayout(model, authentication, "Questionário de Matrícula", "matricula/questionario")) {
                return "redirect:/";
            }
            
            // RECARREGA TODOS OS ATRIBUTOS NECESSÁRIOS
            Curso curso = cursoRepository.findById(matricula.getCurso().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Curso inválido."));

            model.addAttribute("curso", curso);
            model.addAttribute("matricula", matricula);
            model.addAttribute("matriculaId", matricula.getId());
            model.addAttribute("usuario", usuarioLogado);
            model.addAttribute("questionario", questionario); // MANTÉM AS RESPOSTAS DO USUÁRIO
            model.addAttribute("perfis", perfilRepository.findAll());
            model.addAttribute("errorMessage", "Por favor, corrija os erros abaixo.");

            return "layout";
        }

        try {
            // Salva questionário
            questionario.setMatricula(matricula);
            questionarioRepository.save(questionario);

            // Efetiva matrícula se necessário
            if (!matricula.isEfetivada()) {
                matricula.setEfetivada(true);
                matricula.setDataEfetivacao(LocalDateTime.now());
                matriculaRepository.save(matricula);

                // Atualiza vagas
                Curso curso = matricula.getCurso();
                if (curso.getVagas() > 0) {
                    curso.setVagas(curso.getVagas() - 1);
                    cursoRepository.save(curso);
                }

                // ENVIAR EMAIL DE CONFIRMAÇÃO
                try {
                    emailService.enviarConfirmacaoMatricula(matricula);
                } catch (Exception e) {
                    System.err.println("❌ Erro ao enviar email de confirmação: " + e.getMessage());
                    // Não interrompe o fluxo se o email falhar
                }
            }

            return "redirect:/matricula/confirmacao?matriculaId=" + matricula.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao processar matrícula: " + e.getMessage());
            return "redirect:/matricula/nova?cursoId=" + matricula.getCurso().getId();
        }
    }

    // =======================================
    // CONFIRMAÇÃO DE MATRÍCULA
    // =======================================
    @GetMapping("/confirmacao")
    public String confirmacao(@RequestParam Integer matriculaId,
                            Model model,
                            Authentication authentication,
                            HttpServletRequest request) {

        if (!prepareLayout(model, authentication, "Confirmação de Matrícula", "matricula/confirm")) {
            return "redirect:/";
        }

        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada."));

        model.addAttribute("matricula", matricula);
        model.addAttribute("curso", matricula.getCurso());
        model.addAttribute("usuario", matricula.getUsuario());

        return "layout";
    }

    // =======================================
    // APROVAR ALUNO
    // =======================================
    @PostMapping("/{matriculaId}/aprovar")
    @Transactional
    @ResponseBody
    public ResponseEntity<?> aprovarAluno(@PathVariable Integer matriculaId,
                                        HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
        }

        try {
            // Verifica se é admin ou técnico
            if (usuarioLogado.getPerfil().getId() != 1 && usuarioLogado.getPerfil().getId() != 2) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas administradores e técnicos podem aprovar alunos");
            }

            Matricula matricula = matriculaRepository.findById(matriculaId)
                    .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada: " + matriculaId));

            // Verifica se a matrícula está efetivada
            if (!matricula.isEfetivada()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Apenas alunos com matrícula completa podem ser aprovados");
            }

            // Aprova o aluno
            matricula.setAlunoAprovado(true);
            matriculaRepository.save(matricula);

            return ResponseEntity.ok().body("Aluno aprovado com sucesso");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao aprovar aluno: " + e.getMessage());
        }
    }

    // =======================================
    // REPROVAR ALUNO
    // =======================================
    @PostMapping("/{matriculaId}/reprovar")
    @Transactional
    @ResponseBody
    public ResponseEntity<?> reprovarAluno(@PathVariable Integer matriculaId,
                                         HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
        }

        try {
            // Verifica se é admin ou técnico
            if (usuarioLogado.getPerfil().getId() != 1 && usuarioLogado.getPerfil().getId() != 2) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas administradores e técnicos podem reprovar alunos");
            }

            Matricula matricula = matriculaRepository.findById(matriculaId)
                    .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada: " + matriculaId));

            // Reprova o aluno
            matricula.setAlunoAprovado(false);
            matriculaRepository.save(matricula);

            return ResponseEntity.ok().body("Aluno reprovado com sucesso");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao reprovar aluno: " + e.getMessage());
        }
    }

    // =======================================
    // CANCELAR MATRÍCULA (PARA USUÁRIO COMUM)
    // =======================================
    @GetMapping("/cancelar/{matriculaId}")
    @Transactional
    public String cancelarMatricula(@PathVariable Integer matriculaId,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/";
        }

        try {
            Matricula matricula = matriculaRepository.findById(matriculaId)
                    .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada."));

            // Verifica se o usuário tem permissão para cancelar esta matrícula
            if (usuarioLogado.getPerfil().getId() == 3 && 
                !matricula.getUsuario().getId().equals(usuarioLogado.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Você não tem permissão para cancelar esta matrícula.");
                return "redirect:/matricula/minhas-matriculas";
            }

            // Apenas matrículas pendentes podem ser canceladas
            if (matricula.isEfetivada()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Apenas matrículas pendentes podem ser canceladas.");
                return "redirect:/matricula/minhas-matriculas";
            }

            // Remove a matrícula
            matriculaRepository.delete(matricula);

            redirectAttributes.addFlashAttribute("successMessage", "Matrícula cancelada com sucesso!");
            return "redirect:/matricula/minhas-matriculas";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao cancelar matrícula: " + e.getMessage());
            return "redirect:/matricula/minhas-matriculas";
        }
    }

    // =======================================
    // EXCLUSÃO DE MATRÍCULA (PARA ADMIN)
    // =======================================
    @GetMapping("/excluir/{matriculaId}")
    @Transactional
    public String excluirMatricula(@PathVariable Integer matriculaId,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/";
        }

        try {
            Matricula matricula = matriculaRepository.findById(matriculaId)
                    .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada."));

            // Apenas admin pode excluir matrículas
            if (usuarioLogado.getPerfil().getId() != 1) {
                redirectAttributes.addFlashAttribute("errorMessage", "Apenas administradores podem excluir matrículas.");
                return "redirect:/matricula";
            }

            Curso curso = matricula.getCurso();
            if (matricula.isEfetivada()) {
                curso.setVagas(curso.getVagas() + 1);
                cursoRepository.save(curso);
            }

            Questionario questionario = questionarioRepository.findByMatricula(matricula);
            if (questionario != null) {
                questionarioRepository.delete(questionario);
            }
            matriculaRepository.delete(matricula);

            redirectAttributes.addFlashAttribute("successMessage", "Matrícula excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao excluir matrícula: " + e.getMessage());
        }

        return "redirect:/matricula";
    }

    // =======================================
    // MINHAS MATRÍCULAS - PARA USUÁRIO COMUM
    // =======================================
    @GetMapping("/minhas-matriculas")
    public String minhasMatriculas(HttpServletRequest request,
                                 Model model,
                                 Authentication authentication) {
        if (!prepareLayout(model, authentication, "Minhas Matrículas", "matricula/userlist")) {
            return "redirect:/";
        }

        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/";
        }

        // Busca apenas as matrículas do usuário logado
        List<Matricula> minhasMatriculas = matriculaRepository.findByUsuarioId(usuarioLogado.getId(), Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("matriculas", minhasMatriculas);

        return "layout";
    }

    // =======================================
    // DETALHES DA MATRÍCULA
    // =======================================
    @GetMapping("/detalhes/{matriculaId}")
    public String detalhesMatricula(@PathVariable Integer matriculaId,
                                  HttpServletRequest request,
                                  Model model,
                                  Authentication authentication) {
        if (!prepareLayout(model, authentication, "Detalhes da Matrícula", "matricula/detalhes")) {
            return "redirect:/";
        }

        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/";
        }

        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada."));

        // Verifica permissão
        if (usuarioLogado.getPerfil().getId() == 3 && 
            !matricula.getUsuario().getId().equals(usuarioLogado.getId())) {
            return "redirect:/matricula/minhas-matriculas";
        }

        model.addAttribute("matricula", matricula);
        model.addAttribute("curso", matricula.getCurso());
        model.addAttribute("usuario", matricula.getUsuario());

        // Busca questionário se existir
        Questionario questionario = questionarioRepository.findByMatricula(matricula);
        model.addAttribute("questionario", questionario);

        return "layout";
    }

    // =======================================
    // GERAR CERTIFICADO INDIVIDUAL
    // =======================================
    @PostMapping("/gerar-certificado/{matriculaId}")
    @Transactional
    @ResponseBody
    public ResponseEntity<?> gerarCertificadoIndividual(@PathVariable Integer matriculaId,
                                                    HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
        }

        try {
            // Verifica se é admin
            if (usuarioLogado.getPerfil().getId() != 1) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas administradores podem gerar certificados");
            }

            Matricula matricula = matriculaRepository.findById(matriculaId)
                    .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada: " + matriculaId));

            // Verifica se o aluno está aprovado
            if (matricula.getAlunoAprovado() == null || !matricula.getAlunoAprovado()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Aluno não está aprovado no curso");
            }

            // Gera o certificado
            String caminhoCertificado = certificadoService.gerarCertificadoParaMatricula(matricula);

            return ResponseEntity.ok().body("Certificado gerado com sucesso: " + caminhoCertificado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao gerar certificado: " + e.getMessage());
        }
    }

    // =======================================
    // DOWNLOAD DO CERTIFICADO COM CONTROLE DE ACESSO
    // =======================================
    @GetMapping("/download-certificado/{matriculaId}")
    public ResponseEntity<Resource> downloadCertificado(@PathVariable Integer matriculaId,
                                                      HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Obtém o certificado com controle de acesso
            Resource certificado = certificadoService.obterCertificado(matriculaId, usuarioLogado);
            
            // Obtém informações da matrícula para o nome do arquivo
            Matricula matricula = matriculaRepository.findById(matriculaId)
                    .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada."));
            
            String fileName = "certificado_" + 
                matricula.getUsuario().getNome().replaceAll("\\s+", "_") + "_" +
                matricula.getCurso().getNome().replaceAll("\\s+", "_") + ".pdf";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(certificado);
                    
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =======================================
    // VISUALIZAR CERTIFICADO (EMBED NO NAVEGADOR)
    // =======================================
    @GetMapping("/visualizar-certificado/{matriculaId}")
    public ResponseEntity<Resource> visualizarCertificado(@PathVariable Integer matriculaId,
                                                        HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Resource certificado = certificadoService.obterCertificado(matriculaId, usuarioLogado);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"certificado.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(certificado);
                    
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}