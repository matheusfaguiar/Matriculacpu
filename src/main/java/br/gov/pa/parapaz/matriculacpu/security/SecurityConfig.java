package br.gov.pa.parapaz.matriculacpu.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.gov.pa.parapaz.matriculacpu.entity.Usuario;
import br.gov.pa.parapaz.matriculacpu.service.UsuarioService;

@Configuration
public class SecurityConfig {

    @Autowired
    private UsuarioService usuarioService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        Logger logger = LoggerFactory.getLogger(SecurityFilterChain.class);

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/auth/**", "/api/public/**", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/")
                .successHandler((request, response, authentication) -> {
                    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                    String email = oAuth2User.getAttribute("email");
                    String name  = oAuth2User.getAttribute("name");
                    String uid   = oAuth2User.getName();

                    // busca ou cria usuário
                    Usuario usuario = usuarioService.buscarOuCriar(uid, email, name);

                    // Atualiza último login
                    usuario.setUltimoLogin(LocalDateTime.now());
                    usuarioService.salvar(usuario);

                    request.getSession().setAttribute("usuarioLogado", usuario);

                    logger.info("Login realizado - Email: " + usuario.getEmail() + 
                            ", Cadastro Completo: " + usuario.isCadastroCompleto());

                    // redireciona conforme cadastro
                    if (!usuario.isCadastroCompleto()) {
                        logger.info("Redirecionando para completar cadastro");
                        response.sendRedirect("/usuario/completar-cadastro");
                    } else {
                        logger.info("Redirecionando para página principal");
                        response.sendRedirect("/");
                    }
                })
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
