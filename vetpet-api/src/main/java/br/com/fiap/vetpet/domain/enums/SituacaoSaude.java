package br.com.fiap.vetpet.domain.enums;

public enum SituacaoSaude {
    EM_DIA("Acompanhamento em dia", "ok"),
    ATENCAO("Check-up atrasado", "alerta"),
    SEM_HISTORICO("Sem atendimentos", "neutro");

    private final String descricao;
    private final String estilo;

    SituacaoSaude(String descricao, String estilo) {
        this.descricao = descricao;
        this.estilo = estilo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getEstilo() {
        return estilo;
    }
}
