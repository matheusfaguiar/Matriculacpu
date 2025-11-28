package br.gov.pa.parapaz.matriculacpu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sendgrid.SendGrid;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "sendgrid")
@Getter
@Setter
public class SendGridConfig {
    
    private String apiKey;
    private String fromEmail;
    private String fromName;
    
    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(apiKey);
    }
}