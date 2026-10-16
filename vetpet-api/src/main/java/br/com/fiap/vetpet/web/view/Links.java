package br.com.fiap.vetpet.web.view;

import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Objects;

final class Links {

    private Links() {
    }

    static String comFiltros(String caminho, Map<String, ?> filtros) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(caminho);
        filtros.forEach((nome, valor) -> {
            if (valor != null && !Objects.toString(valor).isBlank()) {
                builder.queryParam(nome, valor);
            }
        });
        return builder.encode().build().toUriString();
    }
}
