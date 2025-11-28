package br.gov.pa.parapaz.matriculacpu.controller;

import br.gov.pa.parapaz.matriculacpu.entity.Usuario;
import br.gov.pa.parapaz.matriculacpu.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.HandlerInterceptor;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @ModelAttribute
    public void addUserAttributes(Model model, 
                                  Authentication authentication, 
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws IOException, ServletException {
        if (authentication != null && authentication.isAuthenticated()) {
            OAuth2User user = (OAuth2User) authentication.getPrincipal();
            model.addAttribute("userName", user.getAttribute("name"));
            model.addAttribute("userEmail", user.getAttribute("email"));
            model.addAttribute("userPhoto", user.getAttribute("picture"));
            model.addAttribute("showSidebar", true);

            // Busca usuário na sessão (usando a chave correta)
            Usuario usuario = (Usuario) request.getSession().getAttribute("usuarioLogado");
            if (usuario == null) {
                // busca no banco pelo e-mail do OAuth2User
                String email = user.getAttribute("email");
                Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);
                if (optUsuario.isPresent()) {
                    usuario = optUsuario.get();
                    request.getSession().setAttribute("usuarioLogado", usuario);
                } else {
                    request.getSession().invalidate();
                    request.logout();
                    response.sendRedirect("/logout");
                    return;
                }
            }

            if (usuario != null) {
                model.addAttribute("usuarioLogado", usuario);
            }
        }
    }

    @Component
    public static class CadastroCompletoInterceptor implements HandlerInterceptor {
        
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            // Pega o usuário da sessão
            Usuario usuario = (Usuario) request.getSession().getAttribute("usuarioLogado");
            
            if (usuario != null && !usuario.isCadastroCompleto()) {
                String uri = request.getRequestURI();
                
                // Permite acesso apenas à página de completar cadastro e recursos estáticos
                if (!uri.equals("/usuario/completar-cadastro") && 
                    !uri.startsWith("/logout") &&
                    !uri.startsWith("/css/") &&
                    !uri.startsWith("/js/") &&
                    !uri.equals("/")) {
                    
                    response.sendRedirect("/usuario/completar-cadastro");
                    return false;
                }
            }
            return true;
        }
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model, HttpServletRequest request) {
        // Extrai apenas a informação essencial do erro
        String errorMessage = extractEssentialErrorMessage(ex);
        String rootCause = getRootCauseMessage(ex);
        
        // Log limpo - apenas informações essenciais
        logger.error("Erro processado: {} - URL: {}", errorMessage, request.getRequestURL());
        
        // Log detalhado apenas em modo debug
        if (logger.isDebugEnabled()) {
            //logger.error("Stack trace completo para debugging:", ex);
        }
        
        // Prepara modelo com informações úteis mas não excessivas
        model.addAttribute("status", 500);
        model.addAttribute("error", "Erro interno do servidor");
        model.addAttribute("message", errorMessage);
        model.addAttribute("rootCause", rootCause);
        model.addAttribute("pageTitle", "Erro");
        model.addAttribute("content", "error");
        model.addAttribute("showSidebar", true);
        
        return "layout";
    }

    /**
     * Extrai mensagem de erro essencial sem detalhes técnicos excessivos
     */
    private String extractEssentialErrorMessage(Throwable ex) {
        if (ex == null) {
            return "Erro desconhecido";
        }
        
        // Para exceções comuns do Spring/Security, fornece mensagens mais amigáveis
        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            return "Acesso negado: você não tem permissão para acessar este recurso";
        }
        
        if (ex instanceof org.springframework.web.HttpRequestMethodNotSupportedException) {
            return "Método de requisição não suportado";
        }
        
        if (ex instanceof org.springframework.dao.DataAccessException) {
            return "Erro de acesso aos dados";
        }
        
        // Retorna mensagem da causa raiz quando possível
        Throwable rootCause = getRootCause(ex);
        String message = rootCause.getMessage();
        
        if (message != null && !message.trim().isEmpty()) {
            // Limita o tamanho da mensagem para evitar poluição visual
            if (message.length() > 200) {
                message = message.substring(0, 200) + "...";
            }
            return message;
        }
        
        return rootCause.getClass().getSimpleName();
    }
    
    /**
     * Obtém a causa raiz da exceção
     */
    private Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
    
    /**
     * Obtém mensagem da causa raiz formatada
     */
    private String getRootCauseMessage(Throwable throwable) {
        Throwable rootCause = getRootCause(throwable);
        String message = rootCause.getMessage();
        return message != null ? message : rootCause.getClass().getSimpleName();
    }

    /**
     * Handler específico para exceções de segurança
     */
    @ExceptionHandler(SecurityException.class)
    public String handleSecurityException(SecurityException ex, Model model) {
        logger.warn("Violação de segurança: {}", ex.getMessage());
        
        model.addAttribute("status", 403);
        model.addAttribute("error", "Acesso Negado");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("pageTitle", "Acesso Negado");
        model.addAttribute("content", "error");
        model.addAttribute("showSidebar", true);
        
        return "layout";
    }

    /**
     * Handler específico para exceções de dados não encontrados
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        logger.warn("Recurso não encontrado: {}", ex.getMessage());
        
        model.addAttribute("status", 404);
        model.addAttribute("error", "Recurso Não Encontrado");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("pageTitle", "Não Encontrado");
        model.addAttribute("content", "error");
        model.addAttribute("showSidebar", true);
        
        return "layout";
    }
}