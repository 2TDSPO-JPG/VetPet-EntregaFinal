package br.com.fiap.vetpet.web.api;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

final class Localizacao {

    private Localizacao() {
    }

    static URI recurso(String caminhoBase, Long id) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(caminhoBase + "/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
