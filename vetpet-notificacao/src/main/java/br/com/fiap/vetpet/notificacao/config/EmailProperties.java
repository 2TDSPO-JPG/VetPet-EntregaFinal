package br.com.fiap.vetpet.notificacao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "notificacao.email")
public record EmailProperties(String remetente, List<String> dominiosIgnorados) {

    public boolean configurado() {
        return remetente != null && !remetente.isBlank();
    }

    public boolean deveIgnorar(String email) {
        if (dominiosIgnorados == null || email == null) {
            return false;
        }
        String emailNormalizado = email.trim().toLowerCase();
        return dominiosIgnorados.stream().anyMatch(dominio -> emailNormalizado.endsWith("@" + dominio.trim()));
    }
}
