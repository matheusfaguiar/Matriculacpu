package br.gov.pa.parapaz.matriculacpu.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import br.gov.pa.parapaz.matriculacpu.entity.Matricula;
import br.gov.pa.parapaz.matriculacpu.entity.Usuario;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Service
@ConfigurationProperties(prefix = "app.email")
public class EmailService {
    
    // Enum interno
    public enum EmailProvider {
        GMAIL,
        SENDGRID
    }
    
    // Properties internas  
    private EmailProvider provider = EmailProvider.GMAIL;
    
    public EmailProvider getProvider() {
        return provider;
    }
    
    public void setProvider(EmailProvider provider) {
        this.provider = provider;
    }
    
    // Configurações existentes
    @Value("${spring.mail.username}")
    private String emailFrom;
    
    @Value("${spring.mail.properties.mail.debug:false}")
    private boolean debugEnabled;

    @Value("${sendgrid.from-name:MatrículaCPU - Coordenação da ParáPaz nas Usinas}")
    private String fromName;

    @Value("${sendgrid.api-key:}")
    private String sendGridApiKey;

    @Autowired
    private HttpServletRequest request;
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final SendGrid sendGrid;
    
    public EmailService(JavaMailSender mailSender, 
                       TemplateEngine templateEngine,
                       SendGrid sendGrid) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.sendGrid = sendGrid;
    }
    
    // Método principal que escolhe o provedor automaticamente
    private void enviarEmail(String destinatario, String assunto, String conteudoHtml) {
        switch (this.provider) {
            case SENDGRID:
                enviarEmailSendGrid(destinatario, assunto, conteudoHtml);
                break;
            case GMAIL:
            default:
                enviarEmailGmail(destinatario, assunto, conteudoHtml);
                break;
        }
    }
    
    // Implementação com Gmail (SMTP)
    private void enviarEmailGmail(String destinatario, String assunto, String conteudoHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(conteudoHtml, true);
            helper.setFrom(emailFrom, fromName);
            
            if (debugEnabled) {
                logDebugInfo("GMAIL", destinatario, assunto, emailFrom);
            }
            
            mailSender.send(message);
            System.out.println("✅ Email enviado via Gmail para: " + destinatario);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar email via Gmail: " + e.getMessage());
            throw new RuntimeException("Falha ao enviar email via Gmail para: " + destinatario, e);
        }
    }
    
    // Implementação com SendGrid
    private void enviarEmailSendGrid(String destinatario, String assunto, String conteudoHtml) {
        try {
            Email from = new Email(emailFrom, fromName);
            Email to = new Email(destinatario);
            Content content = new Content("text/html", conteudoHtml);
            Mail mail = new Mail(from, assunto, to, content);
            
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sendGrid.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                if (debugEnabled) {
                    logDebugInfo("SENDGRID", destinatario, assunto, emailFrom);
                    System.out.println("SendGrid Response: " + response.getStatusCode() + " - " + response.getBody());
                }
                System.out.println("✅ Email enviado via SendGrid para: " + destinatario);
            } else {
                throw new RuntimeException("SendGrid retornou status: " + response.getStatusCode() + " - " + response.getBody());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar email via SendGrid: " + e.getMessage());
            // Fallback para Gmail em caso de erro no SendGrid
            System.out.println("🔄 Tentando fallback para Gmail...");
            enviarEmailGmail(destinatario, assunto, conteudoHtml);
        }
    }
    
    // Método público para enviar email com template
    public void enviarEmailTemplate(String destinatario, String assunto, 
                                   String templateName, Map<String, Object> variaveis) {
        try {
            // Processar template
            Context context = new Context();
            context.setVariables(variaveis);
            String htmlContent = templateEngine.process(templateName, context);
            
            enviarEmail(destinatario, assunto, htmlContent);
            
        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar email para: " + destinatario, e);
        }
    }
    
    private void logDebugInfo(String provider, String destinatario, String assunto, String fromEmail) {
        System.out.println("=== 📧 DEBUG EMAIL (" + provider + ") ===");
        System.out.println("From: " + fromEmail);
        System.out.println("To: " + destinatario);
        System.out.println("Subject: " + assunto);
        System.out.println("Provider: " + provider);
        System.out.println("Timestamp: " + LocalDateTime.now());
        System.out.println("======================");
    }

    // Métodos específicos da aplicação
    public void enviarNotificacaoMatricula(String emailDestinatario, String nomeAluno, 
                                          String numeroMatricula) {
        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("nomeAluno", nomeAluno);
        variaveis.put("numeroMatricula", numeroMatricula);
        variaveis.put("dataEnvio", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        enviarEmailTemplate(
            emailDestinatario,
            "Teste de Matrícula - MatriculaCPU",
            "emails/teste",
            variaveis
        );
    }

    public void enviarConfirmacaoMatricula(Matricula matricula) {
        String baseUrl = getBaseUrl();
        try {
            Map<String, Object> variaveis = new HashMap<>();
            variaveis.put("nomeAluno", matricula.getUsuario().getNomeExibicao());
            variaveis.put("numeroMatricula", "MAT" + String.format("%07d", matricula.getId()));
            variaveis.put("nomeCurso", matricula.getCurso().getNomeExibicao());
            variaveis.put("dataMatricula", matricula.getDataEfetivacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            variaveis.put("horarioCurso", matricula.getCurso().getHorario() != null ? matricula.getCurso().getHorario() : "A definir");
            variaveis.put("periodoCurso", matricula.getCurso().getPeriodo() != null ? matricula.getCurso().getPeriodo() : "A definir");
            variaveis.put("localCurso", matricula.getCurso().getLotacao() != null ? matricula.getCurso().getLotacao().getEndereco() : "A definir");
            variaveis.put("baseUrl", baseUrl);
            
            enviarEmailTemplate(
                matricula.getUsuario().getEmail(),
                "Confirmação de Matrícula - " + matricula.getCurso().getNomeExibicao(),
                "emails/confirmacao-matricula",
                variaveis
            );
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar email de confirmação: " + e.getMessage());
            throw new RuntimeException("Falha ao enviar email de confirmação", e);
        }
    }

    public void enviarConfirmacaoCadastro(Usuario usuario) {
        try {
            String baseUrl = getBaseUrl();
            
            Map<String, Object> variaveis = new HashMap<>();
            variaveis.put("nomeExibicao", usuario.getNomeExibicao());
            variaveis.put("dataCadastro", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")));
            variaveis.put("emailUsuario", usuario.getEmail());
            variaveis.put("baseUrl", baseUrl);
            
            enviarEmailTemplate(
                usuario.getEmail(),
                "Confirmação de Cadastro - MatriculaCPU",
                "emails/confirmacao-cadastro",
                variaveis
            );
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar email de confirmação de cadastro: " + e.getMessage());
            throw new RuntimeException("Falha ao enviar email de confirmação de cadastro", e);
        }
    }
    
    private String getBaseUrl() {
        if (request == null) {
            // Em casos onde não há request (jobs assíncronos), faz um fallback inteligente
            return getFallbackBaseUrl();
        }
        
        try {
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();
            
            StringBuilder baseUrl = new StringBuilder();
            baseUrl.append(scheme).append("://").append(serverName);
            
            // Adiciona porta apenas se não for a padrão
            if (("http".equals(scheme) && serverPort != 80) || 
                ("https".equals(scheme) && serverPort != 443)) {
                baseUrl.append(":").append(serverPort);
            }
            
            // Adiciona context path se existir
            if (StringUtils.hasText(contextPath) && !"/".equals(contextPath)) {
                baseUrl.append(contextPath);
            }
            
            return baseUrl.toString();
            
        } catch (Exception e) {
            // Fallback em caso de erro
            return getFallbackBaseUrl();
        }
    }

    private String getFallbackBaseUrl() {
        return "http://localhost:8080";
    }
        
    // Método para verificar qual provedor está ativo
    public String getProvedorAtivo() {
        return this.provider.name();
    }
    
    // Método para mudar o provedor em tempo de execução (opcional)
    public void setProvedor(EmailProvider provider) {
        this.provider = provider;
        System.out.println("🔄 Provedor de email alterado para: " + provider);
    }
}