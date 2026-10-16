package br.com.fiap.vetpet.domain.enums;

public enum Sexo {
    MACHO("Macho"),
    FEMEA("Fêmea");

    private final String descricao;

    Sexo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
