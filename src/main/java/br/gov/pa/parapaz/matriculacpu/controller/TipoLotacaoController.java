package br.gov.pa.parapaz.matriculacpu.controller;

import br.gov.pa.parapaz.matriculacpu.entity.TipoLotacao;
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
@RequestMapping("/tipo-lotacao")
public class TipoLotacaoController extends BaseController {

    private final TipoLotacaoRepository tipoLotacaoRepository;

    public TipoLotacaoController(TipoLotacaoRepository tipoLotacaoRepository) {
        this.tipoLotacaoRepository = tipoLotacaoRepository;
    }

    // =====================
    // LISTAGEM
    // =====================
    @GetMapping
    public String listartipoLotacao(Model model, Authentication authentication) {
        if (!prepareLayout(model, authentication, "Tipos de Local", "tipo-lotacao/list")) {
            return "redirect:/";
        }

        List<TipoLotacao> tipos = tipoLotacaoRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        model.addAttribute("tipos", tipos);

        return "layout"; // renderiza layout.html com fragmento "tipo-lotacao/list"
    }

    // =====================
    // NOVO (fragmento para modal)
    // =====================
    @GetMapping("/novo")
    public String novotipoLotacaoFragment(Model model) {
        model.addAttribute("tipoLotacao", new TipoLotacao());
        // retorna apenas o fragmento do formulário
        return "tipo-lotacao/form :: formFragment";
    }

    @GetMapping("/editar/{id}")
    public String editartipoLotacaoFragment(@PathVariable Integer id, Model model) {
        TipoLotacao tipoLotacao = tipoLotacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("tipoLotacao inválido: " + id));
        model.addAttribute("tipoLotacao", tipoLotacao);
        return "tipo-lotacao/form :: formFragment";
    }


    // =====================
    // SALVAR (criação ou edição)
    // =====================
    @PostMapping("/salvar")
    public String salvartipoLotacao(@Valid @ModelAttribute TipoLotacao tipoLotacao,
                                BindingResult result,
                                Model model,
                                Authentication authentication) {

        if (result.hasErrors()) {
            model.addAttribute("tipoLotacao", tipoLotacao);
            return "tipo-lotacao/form :: formFragment";
        }

        // Checa se o ID já existe
        boolean existe = tipoLotacaoRepository.existsById(tipoLotacao.getId());
        if (existe) {
            // Edição: busca e atualiza
            TipoLotacao existente = tipoLotacaoRepository.findById(tipoLotacao.getId()).get();
            existente.setDescricao(tipoLotacao.getDescricao());
            tipoLotacaoRepository.save(existente);
        } else {
            // Novo registro
            tipoLotacaoRepository.save(tipoLotacao);
        }

        return "redirect:/tipo-lotacao";
    }




    // =====================
    // EXCLUSÃO
    // =====================
    @GetMapping("/excluir/{id}")
    public String excluirtipoLotacao(@PathVariable Integer id) {
        tipoLotacaoRepository.deleteById(id);
        return "redirect:/tipo-lotacao";
    }
}
