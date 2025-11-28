package br.gov.pa.parapaz.matriculacpu.controller;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

/**
 * Classe base para controllers que usam o layout padrão (layout.html)
 */
public abstract class BaseController {

    /**
     * Inicializa o layout definindo o fragmento de conteúdo e o título da página.
     * Retorna true se o usuário estiver autenticado; false caso contrário.
     */
    protected boolean prepareLayout(Model model, Authentication authentication, String pageTitle, String contentFragment) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("content", contentFragment); // nome do fragmento HTML
        return true;
    }
}
