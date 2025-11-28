package br.gov.pa.parapaz.matriculacpu.controller;

import br.gov.pa.parapaz.matriculacpu.entity.Perfil;
import br.gov.pa.parapaz.matriculacpu.repository.PerfilRepository;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/perfil")
public class PerfilController extends BaseController {

    private final PerfilRepository perfilRepository;

    public PerfilController(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    // =====================
    // LISTAGEM
    // =====================
    @GetMapping
    public String listarPerfis(Model model, Authentication authentication) {
        if (!prepareLayout(model, authentication, "Perfis", "perfil/list")) {
            return "redirect:/";
        }

        List<Perfil> perfis = perfilRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        model.addAttribute("perfis", perfis);

        return "layout";
    }

    // =====================
    // NOVO / EDITAR (modal)
    // =====================
    @GetMapping("/novo")
    public String novoPerfilFragment(Model model) {
        model.addAttribute("perfil", new Perfil());
        return "perfil/form :: formFragment";
    }

    @GetMapping("/editar/{id}")
    public String editarPerfilFragment(@PathVariable Integer id, Model model) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Perfil inválido: " + id));
        model.addAttribute("perfil", perfil);
        return "perfil/form :: formFragment";
    }

    // =====================
    // SALVAR
    // =====================
    @PostMapping("/salvar")
    public String salvarPerfil(@Valid @ModelAttribute Perfil perfil,
                               BindingResult result,
                               Model model,
                               Authentication authentication) {

        if (result.hasErrors()) {
            model.addAttribute("perfil", perfil);
            return "perfil/form :: formFragment";
        }

        boolean existe = perfil.getId() != null && perfilRepository.existsById(perfil.getId());
        if (existe) {
            Perfil existente = perfilRepository.findById(perfil.getId()).get();
            existente.setDescricao(perfil.getDescricao());
            perfilRepository.save(existente);
        } else {
            perfilRepository.save(perfil);
        }

        return "redirect:/perfil";
    }

    // =====================
    // EXCLUSÃO
    // =====================
    @GetMapping("/excluir/{id}")
    public String excluirPerfil(@PathVariable Integer id) {
        perfilRepository.deleteById(id);
        return "redirect:/perfil";
    }
}
