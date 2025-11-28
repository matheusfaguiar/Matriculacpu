package br.gov.pa.parapaz.matriculacpu.controller;

import br.gov.pa.parapaz.matriculacpu.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teste")
public class TesteEmailController {
    
    private final EmailService emailService;
    
    public TesteEmailController(EmailService emailService) {
        this.emailService = emailService;
    }
    
    @PostMapping("/email")
    public ResponseEntity<String> testarEmail(@RequestBody TesteEmailRequest request) {
        try {
            emailService.enviarNotificacaoMatricula(
                request.getEmail(),
                request.getNome(),
                request.getMatricula()
            );
            return ResponseEntity.ok("✅ Email enviado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("❌ Erro ao enviar email: " + e.getMessage());
        }
    }
    
    @GetMapping("/email-simples")
    public ResponseEntity<String> testarEmailSimples() {
        try {
            emailService.enviarNotificacaoMatricula(
                "matheusf.aguiar@gmail.com",
                "Matheus",
                "MAT2024000999"
            );
            return ResponseEntity.ok("✅ Email de teste enviado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("❌ Erro: " + e.getMessage());
        }
    }
    
    public static class TesteEmailRequest {
        private String email;
        private String nome;
        private String matricula;
        
        // getters e setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getMatricula() { return matricula; }
        public void setMatricula(String matricula) { this.matricula = matricula; }
    }
}