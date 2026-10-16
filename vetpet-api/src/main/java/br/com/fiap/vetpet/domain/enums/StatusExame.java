package br.com.fiap.vetpet.domain.enums;

public enum StatusExame {
    AGENDADO("Agendado"),
    REALIZADO("Realizado"),
    CANCELADO("Cancelado"),
    ADIADO("Adiado");

    private final String descricao;

    StatusExame(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isFinalizado() {
        return this == REALIZADO || this == CANCELADO;
    }
}
