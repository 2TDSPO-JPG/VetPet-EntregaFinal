package br.com.fiap.vetpet.dto.response;

import br.com.fiap.vetpet.domain.enums.SituacaoSaude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AcompanhamentoPetResponse {

    private PetResponse pet;
    private SituacaoSaude situacaoSaude;
    private LocalDateTime ultimoAtendimento;
    private ExameResponse proximoAtendimento;
    private List<ExameResponse> atendimentos;
    private List<ProntuarioResponse> prontuarios;
    private List<PerguntaResponse> perguntas;
    private List<EventoPetResponse> linhaDoTempo;
}
