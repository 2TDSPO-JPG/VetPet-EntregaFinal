package br.com.fiap.vetpet.domain.enums;

public enum TipoEvento {
    TUTOR_CADASTRADO("tutor.cadastrado", "Tutor cadastrado"),
    PET_CADASTRADO("pet.cadastrado", "Pet cadastrado"),
    PET_ATUALIZADO("pet.atualizado", "Dados do pet atualizados"),
    TUTOR_VINCULADO("pet.tutor-vinculado", "Novo tutor vinculado"),
    EXAME_AGENDADO("exame.agendado", "Atendimento agendado"),
    EXAME_STATUS_ALTERADO("exame.status-alterado", "Status do atendimento alterado"),
    EXAME_CANCELADO("exame.cancelado", "Atendimento cancelado"),
    EXAME_LEMBRETE("exame.lembrete", "Lembrete de atendimento"),
    PRONTUARIO_REGISTRADO("prontuario.registrado", "Prontuário registrado"),
    PERGUNTA_CRIADA("pergunta.criada", "Nova pergunta do tutor"),
    PERGUNTA_RESPONDIDA("pergunta.respondida", "Pergunta respondida");

    private final String routingKey;
    private final String descricao;

    TipoEvento(String routingKey, String descricao) {
        this.routingKey = routingKey;
        this.descricao = descricao;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getDescricao() {
        return descricao;
    }
}
