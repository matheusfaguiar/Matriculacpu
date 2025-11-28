package br.gov.pa.parapaz.matriculacpu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import br.gov.pa.parapaz.matriculacpu.repository.CursoRepository;
import br.gov.pa.parapaz.matriculacpu.repository.LotacaoRepository;
import br.gov.pa.parapaz.matriculacpu.repository.MatriculaRepository;
import br.gov.pa.parapaz.matriculacpu.entity.Usuario;
import br.gov.pa.parapaz.matriculacpu.entity.Matricula;
import br.gov.pa.parapaz.matriculacpu.entity.Curso;

import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MainController {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private LotacaoRepository lotacaoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @GetMapping("/")
    public String index(Model model, Authentication authentication, HttpServletRequest request) {
        model.addAttribute("pageTitle", "Cursos da Fundação ParáPaz nas Usinas");
        model.addAttribute("showSidebar", false);
        model.addAttribute("content", "index");
        
        // Busca cursos disponíveis
        List<Curso> cursosDisponiveis = cursoRepository.findByDataInicioGreaterThanEqualAndVagasGreaterThan(LocalDate.now(), 0);
        model.addAttribute("cursos", cursosDisponiveis);
        model.addAttribute("lotacoes", lotacaoRepository.findAll(Sort.by(Sort.Direction.ASC, "nome")));

        // Se usuário está logado, carrega suas matrículas
        Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
        if (usuarioLogado != null) {
            model.addAttribute("usuarioLogado", usuarioLogado);
            
            // Busca TODAS as matrículas do usuário
            List<Matricula> matriculasUsuario = matriculaRepository.findByUsuario(usuarioLogado);
            
            // Cria um mapa: cursoId -> matrícula (garantindo uma matrícula por curso)
            Map<Integer, Matricula> matriculaPorCurso = matriculasUsuario.stream()
                .collect(Collectors.toMap(
                    matricula -> matricula.getCurso().getId(),
                    matricula -> matricula,
                    // Em caso de duplicata (não deveria acontecer), mantém a primeira
                    (existing, replacement) -> existing
                ));
            
            model.addAttribute("matriculaPorCurso", matriculaPorCurso);
        }
        
        return "layout";
    }

    @GetMapping("/firebaseStatus")
    public String firebaseStatus() {
        try {
            StringBuilder status = new StringBuilder();
            status.append("=== FIREBASE STATUS ===\n");
            
            // Verifica apps inicializados
            List<FirebaseApp> apps = FirebaseApp.getApps();
            status.append("Firebase Apps encontrados: ").append(apps.size()).append("\n");
            
            for (FirebaseApp app : apps) {
                status.append("App: ").append(app.getName()).append("\n");
                status.append("Options: ").append(app.getOptions().getProjectId()).append("\n");
            }
            
            // Tenta obter instância do Auth
            try {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                status.append("FirebaseAuth: OK\n");
            } catch (Exception e) {
                status.append("FirebaseAuth ERROR: ").append(e.getMessage()).append("\n");
            }
            
            return status.toString();
        } catch (Exception e) {
            return "Error checking Firebase status: " + e.getMessage();
        }
    }
}