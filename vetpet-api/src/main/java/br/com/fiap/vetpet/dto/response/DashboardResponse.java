package br.com.fiap.vetpet.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    private long totalTutores;
    private long totalPets;
    private long totalVeterinarios;
    private long atendimentosHoje;
    private long atendimentosAgendados;
    private long perguntasPendentes;
    private List<ExameResponse> proximosAtendimentos;
    private List<PerguntaResponse> perguntasAguardando;
}
