package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.enums.StatusExame;
import br.com.fiap.vetpet.dto.response.DashboardResponse;
import br.com.fiap.vetpet.dto.response.ExameResponse;
import br.com.fiap.vetpet.repository.ExameRepository;
import br.com.fiap.vetpet.repository.PerguntaRepository;
import br.com.fiap.vetpet.repository.PetRepository;
import br.com.fiap.vetpet.repository.TutorRepository;
import br.com.fiap.vetpet.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final TutorRepository tutorRepository;
    private final PetRepository petRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ExameRepository exameRepository;
    private final PerguntaRepository perguntaRepository;
    private final PerguntaService perguntaService;

    public DashboardResponse resumo() {
        LocalDateTime inicioDoDia = LocalDate.now().atStartOfDay();
        return DashboardResponse.builder()
                .totalTutores(tutorRepository.count())
                .totalPets(petRepository.count())
                .totalVeterinarios(veterinarioRepository.count())
                .atendimentosHoje(exameRepository.countByStatusAndDataHoraBetween(StatusExame.AGENDADO,
                        inicioDoDia, inicioDoDia.plusDays(1)))
                .atendimentosAgendados(exameRepository.countByStatus(StatusExame.AGENDADO))
                .perguntasPendentes(perguntaRepository.countByRespostaIsNull())
                .proximosAtendimentos(exameRepository
                        .findTop5ByStatusInAndDataHoraAfterOrderByDataHoraAsc(ExameService.STATUS_EM_ABERTO,
                                LocalDateTime.now())
                        .stream().map(ExameResponse::de).toList())
                .perguntasAguardando(perguntaService.listarPendentes())
                .build();
    }
}
