package br.com.fiap.vetpet.domain.enums;

public enum TipoAtendimento {
    CONSULTA("Consulta"),
    RETORNO("Retorno"),
    VACINA("Vacinação"),
    EXAME_LABORATORIAL("Exame laboratorial"),
    EXAME_IMAGEM("Exame de imagem"),
    CIRURGIA("Cirurgia");

    private final String descricao;

    TipoAtendimento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
