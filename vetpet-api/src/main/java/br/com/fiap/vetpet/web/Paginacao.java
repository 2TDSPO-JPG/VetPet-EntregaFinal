package br.com.fiap.vetpet.web;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Paginacao {

    public static final int TAMANHO_PADRAO = 10;
    private static final int TAMANHO_MAXIMO = 50;

    private Paginacao() {
    }

    public static Pageable de(int pagina, int tamanho, Sort ordenacao) {
        int tamanhoValido = Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO);
        return PageRequest.of(Math.max(pagina, 0), tamanhoValido, ordenacao);
    }
}
