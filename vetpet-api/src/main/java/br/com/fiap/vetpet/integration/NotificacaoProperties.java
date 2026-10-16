package br.com.fiap.vetpet.integration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vetpet.notificacao")
public record NotificacaoProperties(String url, String apiKey) {
}
