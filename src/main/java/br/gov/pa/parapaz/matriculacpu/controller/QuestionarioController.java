// package br.gov.pa.parapaz.matriculacpu.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.ui.Model;
// import org.springframework.validation.BindingResult;
// import org.springframework.security.core.Authentication;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.ModelAttribute;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;

// import br.gov.pa.parapaz.matriculacpu.entity.Matricula;
// import br.gov.pa.parapaz.matriculacpu.entity.Questionario;
// import br.gov.pa.parapaz.matriculacpu.repository.MatriculaRepository;
// import br.gov.pa.parapaz.matriculacpu.repository.QuestionarioRepository;
// import jakarta.validation.Valid;

// public class QuestionarioController extends BaseController {

//     @Autowired
//     MatriculaRepository matriculaRepository;

//     @Autowired
//     QuestionarioRepository questionarioRepository;
    
//     @GetMapping("/questionario/{matriculaId}")
//     public String exibirQuestionario(@PathVariable Long matriculaId, Model model, Authentication authentication) {
//         if (!prepareLayout(model, authentication, "Questionário de Matrícula", "questionario/form")) {
//             return "redirect:/";
//         }
//         model.addAttribute("matriculaId", matriculaId);

//         return "questionario/form";
//     }

//     @PostMapping("/{matriculaId}")
//     public String salvarQuestionario(@PathVariable Integer matriculaId,
//                                     @Valid @ModelAttribute("questionario") Questionario questionario,
//                                     BindingResult bindingResult,
//                                     Model model,
//                                     Authentication authentication) {

//         if (bindingResult.hasErrors()) {
//             // Re-popula a matrícula para o select ou outras infos do form
//             prepareLayout(model, authentication, "Questionário de Matrícula", "questionario/form");
//             return "questionario/form";
//         }

//         Matricula matricula = matriculaRepository.findById(matriculaId)
//                                     .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada"));
//         questionario.setMatricula(matricula);
//         questionarioRepository.save(questionario);

//         matricula.setEfetivada(true);
//         matriculaRepository.save(matricula);

//         return "redirect:/matricula/confirmacao";
//     }
// }
