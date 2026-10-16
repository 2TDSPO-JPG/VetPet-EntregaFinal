package br.com.fiap.vetpet.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vetpet.mensageria")
public record MensageriaProperties(boolean habilitada, String exchange, String filaHistorico) {

    public String exchangeDlx() {
        return exchange + ".dlx";
    }

    public String filaHistoricoDlq() {
        return filaHistorico + ".dlq";
    }
}
