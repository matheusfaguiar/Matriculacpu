package br.gov.pa.parapaz.matriculacpu.controller;

import br.gov.pa.parapaz.matriculacpu.entity.Lotacao;
import br.gov.pa.parapaz.matriculacpu.entity.LotacaoTecnico;
import br.gov.pa.parapaz.matriculacpu.entity.Perfil;
import br.gov.pa.parapaz.matriculacpu.entity.Usuario;
import br.gov.pa.parapaz.matriculacpu.repository.LotacaoRepository;
import br.gov.pa.parapaz.matriculacpu.repository.LotacaoTecnicoRepository;
import br.gov.pa.parapaz.matriculacpu.repository.PerfilRepository;
import br.gov.pa.parapaz.matriculacpu.repository.UsuarioRepository;
import br.gov.pa.parapaz.matriculacpu.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/usuario")
public class UsuarioController extends BaseController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PerfilRepository perfilRepository;
    @Autowired
    private LotacaoRepository lotacaoRepository;
    @Autowired
    private LotacaoTecnicoRepository lotacaoTecnicoRepository;
    @Autowired
    private EmailService emailService;

    /**
     * Endpoint para completar cadastro de usuários temporários, e editar o próprio usuário
     */
    @GetMapping("/completar-cadastro")
    public String showCompleteRegistrationForm(HttpServletRequest request, Model model, Authentication authentication) {
        // Pega o usuário da sessão usando a chave correta
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuarioLogado");
        
        if (usuario == null) {
            return "redirect:/";
        }

        if (!prepareLayout(model, authentication, "Completar Cadastro", "usuario/completar-cadastro")) {
            return "redirect:/";
        }

        model.addAttribute("usuario", usuario);

        List<Perfil> perfis = perfilRepository.findAll();
        model.addAttribute("perfis", perfis);
        model.addAttribute("canEditPerfil", false); //usuário não pode editar seu próprio perfil nesta tela
        model.addAttribute("isModal", false);

        // Buscar e exibir lotação apenas para técnicos (perfil ID 2)
        if (usuario.getPerfil().getId() == 2) {
            List<LotacaoTecnico> lotacoes = lotacaoTecnicoRepository.findByUsuario(usuario);
            if (!lotacoes.isEmpty()) {
                Lotacao lotacao = lotacoes.get(0).getLotacao();
                model.addAttribute("lotacaoTecnico", lotacao);
                model.addAttribute("showLotacao", true);
            } else {
                model.addAttribute("showLotacao", false);
            }
        } else {
            // Admin (ID 1) e Aluno (ID 3) não têm lotação
            model.addAttribute("showLotacao", false);
        }

        return "layout";
    }

     @PostMapping("/completar-cadastro")
    public String completeRegistration(
            @Valid @ModelAttribute("usuario") Usuario dto,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        boolean isPrimeiroCadastro = false;
        
        try {
            // Recupera usuário da sessão usando a chave correta
            Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
            if (usuarioLogado == null) {
                redirectAttributes.addFlashAttribute("error", "Usuário não encontrado na sessão");
                return "redirect:/";
            }

            //VERIFICA SE É O PRIMEIRO CADASTRO COMPLETO
            if (!usuarioLogado.isCadastroCompleto()) {
                isPrimeiroCadastro = true;
            }

            // Atualiza apenas campos do formulário
            usuarioLogado.setNome(dto.getNome());
            usuarioLogado.setNomeSocial(dto.getNomeSocial());
            usuarioLogado.setIdentidadeGenero(dto.getIdentidadeGenero());
            usuarioLogado.setCpf(dto.getCpf());
            usuarioLogado.setTelefone(dto.getTelefone());
            usuarioLogado.setDataNascimento(dto.getDataNascimento());
            usuarioLogado.setRg(dto.getRg());
            usuarioLogado.setRgOrgao(dto.getRgOrgao());
            usuarioLogado.setNaturalidade(dto.getNaturalidade());
            usuarioLogado.setUf(dto.getUf());
            usuarioLogado.setNomeMae(dto.getNomeMae());
            usuarioLogado.setNomePai(dto.getNomePai());
            usuarioLogado.setNIS(dto.getNIS());
            usuarioLogado.setCNS(dto.getCNS());
            
            // Marca como cadastro completo
            // Verifica se todos os campos obrigatórios estão preenchidos
            Boolean cadastroCompleto = usuarioLogado.getNome() != null && !usuarioLogado.getNome().isEmpty()
                && usuarioLogado.getCpf() != null && !usuarioLogado.getCpf().isEmpty()
                && usuarioLogado.getTelefone() != null && !usuarioLogado.getTelefone().isEmpty()
                && usuarioLogado.getDataNascimento() != null
                && usuarioLogado.getRg() != null && !usuarioLogado.getRg().isEmpty()
                && usuarioLogado.getIdentidadeGenero() != null && !usuarioLogado.getIdentidadeGenero().isEmpty()
                && usuarioLogado.getNaturalidade() != null && !usuarioLogado.getNaturalidade().isEmpty()
                && usuarioLogado.getUf() != null && !usuarioLogado.getUf().isEmpty()
                && usuarioLogado.getNomeMae() != null && !usuarioLogado.getNomeMae().isEmpty()
                && usuarioLogado.getNomePai() != null && !usuarioLogado.getNomePai().isEmpty();

            usuarioLogado.setCadastroCompleto(cadastroCompleto);

            usuarioRepository.save(usuarioLogado);

            // Atualiza o usuário na sessão
            request.getSession().setAttribute("usuarioLogado", usuarioLogado);

            // ✅ ENVIA EMAIL DE CONFIRMAÇÃO SE FOR PRIMEIRO CADASTRO COMPLETO
            if (isPrimeiroCadastro && cadastroCompleto) {
                try {
                    emailService.enviarConfirmacaoCadastro(usuarioLogado);
                } catch (Exception e) {
                    System.err.println("❌ Erro ao enviar email de confirmação de cadastro: " + e.getMessage());
                    // Não interrompe o fluxo principal se o email falhar
                }
            }

            redirectAttributes.addFlashAttribute("success", "Cadastro completo com sucesso!");
            return "redirect:/";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao completar cadastro: " + e.getMessage());
            return "redirect:/usuario/completar-cadastro";
        }
    }

    @GetMapping
    public String listUsuarios(Model model, Authentication authentication) {
        if (!prepareLayout(model, authentication, "Usuários", "usuario/list")) {
            return "redirect:/";
        }
        
        List<Usuario> usuarios = usuarioRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        
        // Para cada técnico (perfil ID 2), busca sua lotação
        Map<Integer, Lotacao> lotacoesTecnicos = new HashMap<>();
        for (Usuario usuario : usuarios) {
            if (usuario.getPerfil() != null && usuario.getPerfil().getId() == 2) {
                List<LotacaoTecnico> lotacoes = lotacaoTecnicoRepository.findByUsuario(usuario);
                if (!lotacoes.isEmpty()) {
                    lotacoesTecnicos.put(usuario.getId(), lotacoes.get(0).getLotacao());
                }
            }
        }
        
        // Busca todos os perfis para os filtros
        List<Perfil> perfis = perfilRepository.findAll();
        
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("lotacoesTecnicos", lotacoesTecnicos);
        model.addAttribute("perfis", perfis); // Adiciona os perfis para os filtros
        
        return "layout";
    }

    @GetMapping("/editar/{id}")
    public String editUserModal(@PathVariable Integer id, 
            HttpServletRequest request,
            Model model) {
        
        try {
            // Busca o usuário a ser editado
            Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            // Usuário logado para verificar permissões
            Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
            if (usuarioLogado == null) {
                model.addAttribute("error", "Usuário não encontrado na sessão");
                return "usuario/editar :: modal-content";
            }

            model.addAttribute("usuario", usuario);
            
            List<Perfil> perfis = perfilRepository.findAll();
            model.addAttribute("perfis", perfis);

            // Verifica se usuário logado é admin (perfil ID 1)
            boolean canEditPerfil = usuarioLogado.getPerfil().getId() == 1;
            model.addAttribute("canEditPerfil", canEditPerfil);

            // **CORREÇÃO: Sempre carregar lotações disponíveis se for admin**
            List<Lotacao> lotacoesDisponiveis = List.of();
            if (canEditPerfil) {
                lotacoesDisponiveis = lotacaoRepository.findAll(Sort.by(Sort.Direction.ASC, "nome"));
            }
            model.addAttribute("lotacoesDisponiveis", lotacoesDisponiveis);

            // Busca lotação atual do técnico (se houver)
            if (usuario.getPerfil() != null && usuario.getPerfil().getId() == 2) {
                List<LotacaoTecnico> lotacoesTecnico = lotacaoTecnicoRepository.findByUsuario(usuario);
                if (!lotacoesTecnico.isEmpty()) {
                    model.addAttribute("lotacaoTecnico", lotacoesTecnico.get(0).getLotacao());
                }
            }

            return "usuario/editar :: formFragment";

        } catch (Exception e) {
            model.addAttribute("error", "Erro ao carregar usuário: " + e.getMessage());
            return "usuario/editar :: formFragment";
        }
    }

    @PostMapping("/salvar")
    public String saveOrUpdateUser(
            @Valid @ModelAttribute Usuario dto,
            @RequestParam(value = "lotacaoId", required = false) Integer lotacaoId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            // Usuário logado para verificar permissões
            Usuario usuarioLogado = (Usuario) request.getSession().getAttribute("usuarioLogado");
            if (usuarioLogado == null) {
                redirectAttributes.addFlashAttribute("error", "Usuário não identificado");
                return "redirect:/";
            }

            // Busca o usuário existente no banco
            Usuario usuarioExistente = usuarioRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Atualiza os campos básicos
            usuarioExistente.setNome(dto.getNome());
            usuarioExistente.setNomeSocial(dto.getNomeSocial());
            usuarioExistente.setIdentidadeGenero(dto.getIdentidadeGenero());
            usuarioExistente.setCpf(dto.getCpf());
            usuarioExistente.setTelefone(dto.getTelefone());
            usuarioExistente.setDataNascimento(dto.getDataNascimento());
            usuarioExistente.setRg(dto.getRg());
            usuarioExistente.setRgOrgao(dto.getRgOrgao());
            usuarioExistente.setNaturalidade(dto.getNaturalidade());
            usuarioExistente.setUf(dto.getUf());
            usuarioExistente.setNomeMae(dto.getNomeMae());
            usuarioExistente.setNomePai(dto.getNomePai());
            usuarioExistente.setNIS(dto.getNIS());
            usuarioExistente.setCNS(dto.getCNS());

            // CORREÇÃO: Só permite editar perfil se for admin
            if (usuarioLogado.getPerfil().getId() == 1 && dto.getPerfil() != null) {
                usuarioExistente.setPerfil(dto.getPerfil());

                // Se o perfil for técnico (ID 2) e foi enviada uma lotação, atualiza
                if (dto.getPerfil().getId() == 2 && lotacaoId != null) {
                    Lotacao lotacao = lotacaoRepository.findById(lotacaoId)
                        .orElseThrow(() -> new RuntimeException("Lotação inválida"));
                    
                    // Busca ou cria a relação de lotação do técnico
                    List<LotacaoTecnico> lotacoesExistentes = lotacaoTecnicoRepository.findByUsuario(usuarioExistente);
                    if (!lotacoesExistentes.isEmpty()) {
                        // Atualiza lotação existente
                        LotacaoTecnico lotacaoTecnico = lotacoesExistentes.get(0);
                        lotacaoTecnico.setLotacao(lotacao);
                        lotacaoTecnicoRepository.save(lotacaoTecnico);
                    } else {
                        // Cria nova relação
                        LotacaoTecnico novaRelacao = new LotacaoTecnico(usuarioExistente, lotacao);
                        lotacaoTecnicoRepository.save(novaRelacao);
                    }
                } else if (dto.getPerfil().getId() != 2) {
                    // Se não é mais técnico, remove a lotação
                    List<LotacaoTecnico> lotacoesExistentes = lotacaoTecnicoRepository.findByUsuario(usuarioExistente);
                    lotacaoTecnicoRepository.deleteAll(lotacoesExistentes);
                }
            }

            // Verifica se cadastro está completo
            boolean cadastroCompleto = usuarioExistente.getNome() != null && !usuarioExistente.getNome().isEmpty()
                && usuarioExistente.getCpf() != null && !usuarioExistente.getCpf().isEmpty()
                && usuarioExistente.getTelefone() != null && !usuarioExistente.getTelefone().isEmpty()
                && usuarioExistente.getDataNascimento() != null
                && usuarioExistente.getRg() != null && !usuarioExistente.getRg().isEmpty()
                && usuarioExistente.getIdentidadeGenero() != null && !usuarioExistente.getIdentidadeGenero().isEmpty()
                && usuarioExistente.getNaturalidade() != null && !usuarioExistente.getNaturalidade().isEmpty()
                && usuarioExistente.getUf() != null && !usuarioExistente.getUf().isEmpty()
                && usuarioExistente.getNomeMae() != null && !usuarioExistente.getNomeMae().isEmpty()
                && usuarioExistente.getNomePai() != null && !usuarioExistente.getNomePai().isEmpty();

            usuarioExistente.setCadastroCompleto(cadastroCompleto);

            usuarioRepository.save(usuarioExistente);

            redirectAttributes.addFlashAttribute("success", "Usuário atualizado com sucesso!");
            return "redirect:/usuario";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao salvar usuário: " + e.getMessage());
            return "redirect:/usuario";
        }
    }

}