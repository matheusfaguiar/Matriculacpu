package br.gov.pa.parapaz.matriculacpu.controller;

import br.gov.pa.parapaz.matriculacpu.entity.Curso;
import br.gov.pa.parapaz.matriculacpu.entity.Lotacao;
import br.gov.pa.parapaz.matriculacpu.entity.LotacaoTecnico;
import br.gov.pa.parapaz.matriculacpu.entity.Matricula;
import br.gov.pa.parapaz.matriculacpu.entity.TipoCurso;
import br.gov.pa.parapaz.matriculacpu.entity.Usuario;
import br.gov.pa.parapaz.matriculacpu.repository.CursoRepository;
import br.gov.pa.parapaz.matriculacpu.repository.LotacaoRepository;
import br.gov.pa.parapaz.matriculacpu.repository.LotacaoTecnicoRepository;
import br.gov.pa.parapaz.matriculacpu.repository.MatriculaRepository;
import br.gov.pa.parapaz.matriculacpu.repository.TipoCursoRepository;
import br.gov.pa.parapaz.matriculacpu.service.CertificadoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/curso")
public class CursoController extends BaseController {

    @Autowired
    private CursoRepository cursoRepository;
    @Autowired
    private LotacaoRepository lotacaoRepository;
    @Autowired
    private LotacaoTecnicoRepository lotacaoTecnicoRepository;
    @Autowired
    private MatriculaRepository matriculaRepository;
    @Autowired
    private TipoCursoRepository tipoCursoRepository;
    @Autowired
    private CertificadoService certificadoService;

    
    // =====================
    // LISTAGEM COM FILTRO POR PERFIL
    // =====================
    @GetMapping
    public String listarCursos(Model model, Authentication authentication, HttpServletRequest request) {
        if (!prepareLayout(model, authentication, "Cursos", "curso/list")) {
            return "redirect:/";
        }

        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        model.addAttribute("usuarioLogado", usuarioLogado);

        List<Curso> cursos;
        List<Lotacao> lotacoesDisponiveis;

        // Lógica baseada no perfil
        if (usuarioLogado.getPerfil().getId() == 1) { // Admin - vê tudo
            cursos = cursoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            lotacoesDisponiveis = lotacaoRepository.findAll();
        } else if (usuarioLogado.getPerfil().getId() == 2) { // Técnico - vê apenas sua lotação
            List<LotacaoTecnico> lotacoesTecnico = lotacaoTecnicoRepository.findByUsuario(usuarioLogado);
            
            if (lotacoesTecnico.isEmpty()) {
                // Técnico sem lotação definida
                model.addAttribute("semLotacao", true);
                cursos = List.of();
                lotacoesDisponiveis = List.of();
            } else {
                // Pega a primeira lotação (assumindo que técnico tem apenas uma)
                Lotacao lotacaoTecnico = lotacoesTecnico.get(0).getLotacao();
                cursos = cursoRepository.findByLotacao(lotacaoTecnico, Sort.by(Sort.Direction.DESC, "id"));
                lotacoesDisponiveis = List.of(lotacaoTecnico);
                model.addAttribute("lotacaoTecnico", lotacaoTecnico);
            }
        } else { // Outros perfis
            cursos = List.of();
            lotacoesDisponiveis = List.of();
        }

        // Verifica quais cursos têm certificados gerados
        for (Curso curso : cursos) {
            boolean temCertificados = matriculaRepository.findByCursoId((long) curso.getId())
                    .stream()
                    .anyMatch(m -> m.getUrlCertificado() != null && !m.getUrlCertificado().isEmpty());
            curso.setCertificadosGerados(temCertificados);
        }
        
        model.addAttribute("cursos", cursos);
        model.addAttribute("lotacoes", lotacoesDisponiveis);
        
        return "layout";
    }

    // =====================
    // NOVO CURSO - TÉCNICO USA APENAS SUA LOTAÇÃO
    // =====================
    @GetMapping("/novo")
    public String novoCursoFragment(Model model, HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        Curso novoCurso = new Curso();
        
        List<Lotacao> lotacoesDisponiveis;
        
        if (usuarioLogado.getPerfil().getId() == 1) { // Admin - pode escolher qualquer lotação
            lotacoesDisponiveis = lotacaoRepository.findAll(Sort.by(Sort.Direction.ASC, "nome"));
        } else if (usuarioLogado.getPerfil().getId() == 2) { // Técnico - usa apenas sua lotação
            List<LotacaoTecnico> lotacoesTecnico = lotacaoTecnicoRepository.findByUsuario(usuarioLogado);
            
            if (lotacoesTecnico.isEmpty()) {
                // Técnico sem lotação - não pode criar cursos
                model.addAttribute("error", "Sua lotação não foi definida. Entre em contato com o administrador.");
                model.addAttribute("tiposCurso", tipoCursoRepository.findAll(Sort.by(Sort.Direction.ASC, "id")));
                model.addAttribute("lotacoes", List.of());
                return "curso/form :: formFragment";
            }
            
            // Usa a primeira lotação do técnico
            Lotacao lotacaoTecnico = lotacoesTecnico.get(0).getLotacao();
            novoCurso.setLotacao(lotacaoTecnico);
            lotacoesDisponiveis = List.of(lotacaoTecnico);
            model.addAttribute("lotacaoFixa", true); // Flag para desabilitar o campo no frontend
        } else {
            lotacoesDisponiveis = List.of();
        }

        model.addAttribute("curso", novoCurso);
        model.addAttribute("tiposCurso", tipoCursoRepository.findAll(Sort.by(Sort.Direction.ASC, "id")));
        model.addAttribute("lotacoes", lotacoesDisponiveis);
        return "curso/form :: formFragment";
    }

    // =====================
    // EDITAR CURSO - COM VERIFICAÇÃO DE PERMISSÃO
    // =====================
    @GetMapping("/editar/{id}")
    public String editarCursoFragment(@PathVariable Integer id, Model model, HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso inválido: " + id));
        
        // Verifica se o técnico tem permissão para editar este curso
        if (usuarioLogado.getPerfil().getId() == 2) {
            List<LotacaoTecnico> lotacoesTecnico = lotacaoTecnicoRepository.findByUsuario(usuarioLogado);
            
            if (lotacoesTecnico.isEmpty()) {
                throw new SecurityException("Acesso negado: sua lotação não foi definida");
            }
            
            Lotacao lotacaoTecnico = lotacoesTecnico.get(0).getLotacao();
            if (!curso.getLotacao().getId().equals(lotacaoTecnico.getId())) {
                throw new SecurityException("Acesso negado: você não tem permissão para editar este curso");
            }
        }

        List<Lotacao> lotacoesDisponiveis;
        
        if (usuarioLogado.getPerfil().getId() == 1) { // Admin - pode escolher qualquer lotação
            lotacoesDisponiveis = lotacaoRepository.findAll(Sort.by(Sort.Direction.ASC, "nome"));
        } else { // Técnico - usa apenas sua lotação
            List<LotacaoTecnico> lotacoesTecnico = lotacaoTecnicoRepository.findByUsuario(usuarioLogado);
            Lotacao lotacaoTecnico = lotacoesTecnico.get(0).getLotacao();
            lotacoesDisponiveis = List.of(lotacaoTecnico);
            model.addAttribute("lotacaoFixa", true); // Flag para desabilitar o campo no frontend
        }

        model.addAttribute("curso", curso);
        model.addAttribute("tiposCurso", tipoCursoRepository.findAll(Sort.by(Sort.Direction.ASC, "id")));
        model.addAttribute("lotacoes", lotacoesDisponiveis);
        return "curso/form :: formFragment";
    }

    // =====================
    // SALVAR - COM VALIDAÇÃO DE PERMISSÃO
    // =====================
    @PostMapping("/salvar")
    public String salvarCurso(@Valid @ModelAttribute Curso curso,
                              BindingResult result,
                              Model model,
                              Authentication authentication,
                              HttpServletRequest request) {

        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        
        // Para técnicos, força a lotação deles
        if (usuarioLogado.getPerfil().getId() == 2) {
            List<LotacaoTecnico> lotacoesTecnico = lotacaoTecnicoRepository.findByUsuario(usuarioLogado);
            
            if (lotacoesTecnico.isEmpty()) {
                model.addAttribute("error", "Sua lotação não foi definida. Entre em contato com o administrador.");
                model.addAttribute("tiposCurso", tipoCursoRepository.findAll(Sort.by(Sort.Direction.ASC, "id")));
                model.addAttribute("lotacoes", List.of());
                return "curso/form :: formFragment";
            }
            
            // Sobrescreve a lotação com a do técnico (segurança)
            Lotacao lotacaoTecnico = lotacoesTecnico.get(0).getLotacao();
            curso.setLotacao(lotacaoTecnico);
        }

        // ... resto do método original ...
        TipoCurso tipoCurso = tipoCursoRepository.findById(
            curso.getTipoCurso().getId()
        ).orElseThrow();

        Lotacao lotacao = lotacaoRepository.findById(
            curso.getLotacao().getId()
        ).orElseThrow();

        curso.setTipoCurso(tipoCurso);
        curso.setLotacao(lotacao);

        if (result.hasErrors()) {
            List<Lotacao> lotacoesDisponiveis;
            
            if (usuarioLogado.getPerfil().getId() == 1) {
                lotacoesDisponiveis = lotacaoRepository.findAll(Sort.by(Sort.Direction.ASC, "nome"));
            } else {
                List<LotacaoTecnico> lotacoesTecnico = lotacaoTecnicoRepository.findByUsuario(usuarioLogado);
                Lotacao lotacaoTecnico = lotacoesTecnico.get(0).getLotacao();
                lotacoesDisponiveis = List.of(lotacaoTecnico);
            }
            
            model.addAttribute("tiposCurso", tipoCursoRepository.findAll(Sort.by(Sort.Direction.ASC, "id")));
            model.addAttribute("lotacoes", lotacoesDisponiveis);
            return "curso/form :: formFragment";
        }

        cursoRepository.save(curso);
        return "redirect:/curso";
    }

    // =====================
    // EXCLUSÃO - COM VERIFICAÇÃO DE PERMISSÃO
    // =====================
    @GetMapping("/excluir/{id}")
    public String excluirCurso(@PathVariable Integer id, HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso inválido: " + id));
        
        // Verifica se o técnico tem permissão para excluir este curso
        if (usuarioLogado.getPerfil().getId() == 2) {
            List<LotacaoTecnico> lotacoesTecnico = lotacaoTecnicoRepository.findByUsuario(usuarioLogado);
            
            if (lotacoesTecnico.isEmpty()) {
                throw new SecurityException("Acesso negado: sua lotação não foi definida");
            }
            
            Lotacao lotacaoTecnico = lotacoesTecnico.get(0).getLotacao();
            if (!curso.getLotacao().getId().equals(lotacaoTecnico.getId())) {
                throw new SecurityException("Acesso negado: você não tem permissão para excluir este curso");
            }
        }

        cursoRepository.deleteById(id);
        return "redirect:/curso";
    }

    @GetMapping("/aprovar/{cursoId}")
    public String aprovarAlunosDoCurso(@PathVariable Integer cursoId,
                                    Model model,
                                    Authentication authentication,
                                    HttpServletRequest request) {

        if (!prepareLayout(model, authentication, "Aprovação de Alunos", "curso/aprovar")) {
            return "redirect:/";
        }

        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso inválido: " + cursoId));

        // Lista TODAS as matrículas do curso
        List<Matricula> matriculas = matriculaRepository
                .findByCursoIdOrderByEfetivadaDescUsuarioNomeAsc(cursoId);

        model.addAttribute("curso", curso);
        model.addAttribute("matriculas", matriculas);
        model.addAttribute("usuarioLogado", usuarioLogado);

        return "layout";
    }


    @GetMapping("/certificado/{matriculaId}")
    public ResponseEntity<Resource> downloadCertificado(
            @PathVariable Integer matriculaId,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        Resource resource = certificadoService.obterCertificado(matriculaId, usuarioLogado);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }


    // =====================
    // GERAR CERTIFICADOS
    // =====================
    @PostMapping("/gerar-certificado/{cursoId}")
    @ResponseBody
    public List<Map<String, String>> gerarCertificadosDoCursoAjax(@PathVariable Integer cursoId) {
        try {
            Curso curso = cursoRepository.findById(cursoId)
                    .orElseThrow(() -> new IllegalArgumentException("Curso inválido: " + cursoId));
            
            // Retorna a lista de alunos para o front-end mostrar o progresso
            return certificadoService.gerarCertificadosDoCurso(curso);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao gerar certificados: " + e.getMessage());
        }
    }

    /**
     * Preview do certificado de uma matrícula específica
     */
    @PostMapping("/preview-certificado")
    public ResponseEntity<byte[]> previewCertificado(@RequestBody Curso curso) {
        try {
            curso.setTipoCurso(tipoCursoRepository.findById(
                curso.getTipoCurso().getId()
            ).orElseThrow());

            curso.setLotacao(lotacaoRepository.findById(
                curso.getLotacao().getId()
            ).orElseThrow());

            byte[] pdfBytes = certificadoService.gerarPreviewCertificado(curso);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"preview_certificado.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }


}
