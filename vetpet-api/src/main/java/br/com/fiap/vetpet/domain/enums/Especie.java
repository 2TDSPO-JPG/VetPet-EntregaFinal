package br.com.fiap.vetpet.domain.enums;

public enum Especie {
    CACHORRO("Cachorro"),
    GATO("Gato"),
    AVE("Ave"),
    ROEDOR("Roedor"),
    REPTIL("Réptil"),
    OUTRO("Outro");

    private final String descricao;

    Especie(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
