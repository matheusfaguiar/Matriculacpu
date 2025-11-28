package br.gov.pa.parapaz.matriculacpu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import br.gov.pa.parapaz.matriculacpu.controller.GlobalControllerAdvice.CadastroCompletoInterceptor;

import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private CadastroCompletoInterceptor cadastroCompletoInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cadastroCompletoInterceptor)
                .excludePathPatterns("/", "/auth/**", "/api/public/**", "/css/**", "/js/**");
    }
}
