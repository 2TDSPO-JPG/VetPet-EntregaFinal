package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.enums.SituacaoSaude;
import br.com.fiap.vetpet.dto.response.AcompanhamentoPetResponse;
import br.com.fiap.vetpet.repository.ProntuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcompanhamentoService {

    static final int MESES_PARA_CHECKUP = 12;

    private final PetService petService;
    private final ExameService exameService;
    private final ProntuarioService prontuarioService;
    private final PerguntaService perguntaService;
    private final HistoricoService historicoService;
    private final ProntuarioRepository prontuarioRepository;

    public AcompanhamentoPetResponse montar(Long petId) {
        LocalDateTime ultimoAtendimento = prontuarioRepository.ultimoAtendimento(petId).orElse(null);
        return AcompanhamentoPetResponse.builder()
                .pet(petService.buscar(petId))
                .situacaoSaude(avaliar(ultimoAtendimento))
                .ultimoAtendimento(ultimoAtendimento)
                .proximoAtendimento(exameService.buscarProximoDoPet(petId).orElse(null))
                .atendimentos(exameService.listarDoPet(petId))
                .prontuarios(prontuarioService.listarDoPet(petId))
                .perguntas(perguntaService.listarDoPet(petId))
                .linhaDoTempo(historicoService.linhaDoTempo(petId))
                .build();
    }

    static SituacaoSaude avaliar(LocalDateTime ultimoAtendimento) {
        if (ultimoAtendimento == null) {
            return SituacaoSaude.SEM_HISTORICO;
        }
        boolean emDia = ultimoAtendimento.isAfter(LocalDateTime.now().minusMonths(MESES_PARA_CHECKUP));
        return emDia ? SituacaoSaude.EM_DIA : SituacaoSaude.ATENCAO;
    }
}
