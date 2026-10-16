package br.com.fiap.vetpet.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Slf4j
@Component
public class NotificacaoClient {

    private final RestClient restClient;

    public NotificacaoClient(NotificacaoProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(8));
        this.restClient = RestClient.builder()
                .baseUrl(properties.url())
                .defaultHeader("X-API-KEY", properties.apiKey())
                .requestFactory(requestFactory)
                .build();
    }

    public ConsultaNotificacoes listar(String email, int pagina, int tamanho) {
        try {
            PaginaNotificacoes resultado = restClient.get()
                    .uri(uri -> uri.path("/api/notificacoes")
                            .queryParam("email", email == null ? "" : email)
                            .queryParam("pagina", pagina)
                            .queryParam("tamanho", tamanho)
                            .build())
                    .retrieve()
                    .body(PaginaNotificacoes.class);
            return resultado == null ? ConsultaNotificacoes.indisponivel() : ConsultaNotificacoes.de(resultado);
        } catch (RestClientException e) {
            log.warn("Serviço de notificações indisponível: {}", e.getMessage());
            return ConsultaNotificacoes.indisponivel();
        }
    }
}
