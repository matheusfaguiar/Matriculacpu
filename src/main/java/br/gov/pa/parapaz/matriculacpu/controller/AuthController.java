package br.gov.pa.parapaz.matriculacpu.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;

import java.io.IOException;

@Controller
public class AuthController {

    @GetMapping("/auth/google")
    public void authGoogle(HttpServletResponse response) throws IOException {
        // usa fluxo OAuth2 padrão do Spring
        response.sendRedirect("/oauth2/authorization/google");
    }


    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/";
    }
}
