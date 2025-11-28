package br.gov.pa.parapaz.matriculacpu.controller;

import br.gov.pa.parapaz.matriculacpu.entity.TipoCurso;
import br.gov.pa.parapaz.matriculacpu.repository.TipoCursoRepository;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/tipo-curso")
public class TipoCursoController extends BaseController {

    private final TipoCursoRepository tipoCursoRepository;

    public TipoCursoController(TipoCursoRepository tipoCursoRepository) {
        this.tipoCursoRepository = tipoCursoRepository;
    }

    // =====================
    // LISTAGEM
    // =====================
    @GetMapping
    public String listarTipoCurso(Model model, Authentication authentication) {
        if (!prepareLayout(model, authentication, "Tipos de Curso", "tipo-curso/list")) {
            return "redirect:/";
        }

        List<TipoCurso> tipos = tipoCursoRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        model.addAttribute("tipos", tipos);

        return "layout";
    }

    // =====================
    // NOVO / EDITAR (modal)
    // =====================
    @GetMapping("/novo")
    public String novoTipoCursoFragment(Model model) {
        model.addAttribute("tipoCurso", new TipoCurso());
        return "tipo-curso/form :: formFragment";
    }

    @GetMapping("/editar/{id}")
    public String editarTipoCursoFragment(@PathVariable Integer id, Model model) {
        TipoCurso tipoCurso = tipoCursoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TipoCurso inválido: " + id));
        model.addAttribute("tipoCurso", tipoCurso);
        return "tipo-curso/form :: formFragment";
    }

    // =====================
    // SALVAR
    // =====================
    @PostMapping("/salvar")
    public String salvarTipoCurso(@Valid @ModelAttribute TipoCurso tipoCurso,
                                  BindingResult result,
                                  Model model,
                                  Authentication authentication) {

        if (result.hasErrors()) {
            model.addAttribute("tipoCurso", tipoCurso);
            return "tipo-curso/form :: formFragment";
        }

        boolean existe = tipoCurso.getId() != null && tipoCursoRepository.existsById(tipoCurso.getId());
        if (existe) {
            TipoCurso existente = tipoCursoRepository.findById(tipoCurso.getId()).get();
            existente.setNome(tipoCurso.getNome());
            tipoCursoRepository.save(existente);
        } else {
            tipoCursoRepository.save(tipoCurso);
        }

        return "redirect:/tipo-curso";
    }

    // =====================
    // EXCLUSÃO
    // =====================
    @GetMapping("/excluir/{id}")
    public String excluirTipoCurso(@PathVariable Integer id) {
        tipoCursoRepository.deleteById(id);
        return "redirect:/tipo-curso";
    }
}
