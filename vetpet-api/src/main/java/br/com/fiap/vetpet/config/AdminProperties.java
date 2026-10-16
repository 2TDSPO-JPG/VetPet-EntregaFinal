package br.com.fiap.vetpet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vetpet.admin")
public record AdminProperties(String email, String senha) {
}
