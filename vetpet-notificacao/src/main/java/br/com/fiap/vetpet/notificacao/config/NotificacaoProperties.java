package br.com.fiap.vetpet.notificacao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notificacao")
public record NotificacaoProperties(String apiKey, String exchange, String fila) {

    public String exchangeDlx() {
        return exchange + ".dlx";
    }

    public String filaDlq() {
        return fila + ".dlq";
    }
}
