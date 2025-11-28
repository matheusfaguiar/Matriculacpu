package br.gov.pa.parapaz.matriculacpu.controller;

import br.gov.pa.parapaz.matriculacpu.entity.Lotacao;
import br.gov.pa.parapaz.matriculacpu.repository.LotacaoRepository;
import br.gov.pa.parapaz.matriculacpu.repository.TipoLotacaoRepository;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/lotacao")
public class LotacaoController extends BaseController {

    private final LotacaoRepository lotacaoRepository;
    private final TipoLotacaoRepository tipoLotacaoRepository;

    public LotacaoController(LotacaoRepository lotacaoRepository,
                             TipoLotacaoRepository tipoLotacaoRepository) {
        this.lotacaoRepository = lotacaoRepository;
        this.tipoLotacaoRepository = tipoLotacaoRepository;
    }

    // =====================
    // LISTAGEM
    // =====================
    @GetMapping
    public String listarLotacoes(Model model, Authentication authentication) {
        if (!prepareLayout(model, authentication, "Lotações", "lotacao/list")) {
            return "redirect:/";
        }

        List<Lotacao> lotacoes = lotacaoRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        model.addAttribute("lotacoes", lotacoes);

        return "layout";
    }

    // =====================
    // NOVO / EDITAR (modal)
    // =====================
    @GetMapping(value = "/novo", produces = "text/html")
    public String novoLotacaoFragment(Model model) {
        model.addAttribute("lotacao", new Lotacao());
        model.addAttribute("tipos", tipoLotacaoRepository.findAll());
        return "lotacao/form :: formFragment";
    }

    @GetMapping(value = "/editar/{id}", produces = "text/html")
    public String editarLotacaoFragment(@PathVariable Integer id, Model model) {
        Lotacao lotacao = lotacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lotação inválida: " + id));

        model.addAttribute("lotacao", lotacao);
        model.addAttribute("tipos", tipoLotacaoRepository.findAll());
        return "lotacao/form :: formFragment";
    }

    @PostMapping("/salvar")
    public String salvarLotacao(@Valid @ModelAttribute Lotacao lotacao,
                                BindingResult result,
                                Model model,
                                Authentication authentication) {

        if (result.hasErrors()) {
            model.addAttribute("tipos", tipoLotacaoRepository.findAll());
            model.addAttribute("lotacao", lotacao);
            return "lotacao/form :: formFragment";
        }

        // Carregar o TipoLotacao pelo ID antes de salvar
        if (lotacao.getTipoLotacao() != null && lotacao.getTipoLotacao().getId() != null) {
            lotacao.setTipoLotacao(
                tipoLotacaoRepository.findById(lotacao.getTipoLotacao().getId())
                .orElse(null)
            );
        }

        lotacaoRepository.save(lotacao);
        return "redirect:/lotacao";
    }


    // =====================
    // EXCLUSÃO
    // =====================
    @GetMapping("/excluir/{id}")
    public String excluirLotacao(@PathVariable Integer id) {
        lotacaoRepository.deleteById(id);
        return "redirect:/lotacao";
    }
}
