package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.entity.Exame;
import br.com.fiap.vetpet.domain.entity.Pet;
import br.com.fiap.vetpet.domain.entity.Veterinario;
import br.com.fiap.vetpet.domain.enums.StatusExame;
import br.com.fiap.vetpet.domain.enums.TipoEvento;
import br.com.fiap.vetpet.dto.request.ExameRequest;
import br.com.fiap.vetpet.dto.response.ExameResponse;
import br.com.fiap.vetpet.exception.DadoDuplicadoException;
import br.com.fiap.vetpet.exception.RecursoNaoEncontradoException;
import br.com.fiap.vetpet.exception.RegraNegocioException;
import br.com.fiap.vetpet.messaging.EventoPublisher;
import br.com.fiap.vetpet.messaging.EventoVetPet;
import br.com.fiap.vetpet.repository.ExameRepository;
import br.com.fiap.vetpet.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExameService {

    static final Set<StatusExame> STATUS_EM_ABERTO = Set.of(StatusExame.AGENDADO, StatusExame.ADIADO);
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final String RECURSO = "Atendimento";

    private final ExameRepository exameRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final PetService petService;
    private final EventoPublisher eventoPublisher;

    public Page<ExameResponse> listar(StatusExame status, Long petId, Long veterinarioId, LocalDate data,
                                      Pageable pageable) {
        LocalDateTime inicio = data != null ? data.atStartOfDay() : null;
        LocalDateTime fim = data != null ? data.plusDays(1).atStartOfDay() : null;
        return exameRepository.filtrar(status, petId, veterinarioId, inicio, fim, pageable).map(ExameResponse::de);
    }

    public ExameResponse buscar(Long id) {
        return ExameResponse.de(encontrar(id));
    }

    public List<ExameResponse> listarDoPet(Long petId) {
        return exameRepository.findByPetIdOrderByDataHoraDesc(petId).stream().map(ExameResponse::de).toList();
    }

    public List<ExameResponse> listarDoTutor(String email) {
        return exameRepository.findByTutorEmail(email).stream().map(ExameResponse::de).toList();
    }

    public Optional<ExameResponse> buscarProximoDoPet(Long petId) {
        return exameRepository
                .findFirstByPetIdAndStatusInAndDataHoraAfterOrderByDataHoraAsc(petId, STATUS_EM_ABERTO, LocalDateTime.now())
                .map(ExameResponse::de);
    }

    @Transactional
    public ExameResponse agendar(ExameRequest request) {
        if (!request.getDataHora().isAfter(LocalDateTime.now())) {
            throw new RegraNegocioException("O atendimento deve ser agendado para uma data futura.");
        }
        Pet pet = petService.encontrar(request.getPetId());
        Veterinario veterinario = veterinarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veterinário", request.getVeterinarioId()));
        if (exameRepository.existsByVeterinarioIdAndDataHoraAndStatusIn(veterinario.getId(), request.getDataHora(),
                STATUS_EM_ABERTO)) {
            throw new DadoDuplicadoException(veterinario.getNome() + " já possui um atendimento neste horário.");
        }
        Exame exame = new Exame();
        exame.setPet(pet);
        exame.setVeterinario(veterinario);
        exame.setTipo(request.getTipo());
        exame.setDataHora(request.getDataHora());
        exame.setMotivo(DadosPessoais.vazioParaNulo(request.getMotivo()));
        exame.setStatus(StatusExame.AGENDADO);
        Exame salvo = exameRepository.save(exame);
        publicar(TipoEvento.EXAME_AGENDADO, salvo, salvo.getTipo().getDescricao() + " de " + pet.getNome()
                + " agendado(a) para " + salvo.getDataHora().format(FORMATO_DATA) + " com " + veterinario.getNome() + ".");
        return ExameResponse.de(salvo);
    }

    @Transactional
    public ExameResponse alterarStatus(Long id, StatusExame novoStatus) {
        Exame exame = encontrar(id);
        if (exame.getStatus().isFinalizado()) {
            throw new RegraNegocioException("Este atendimento já está " + exame.getStatus().getDescricao().toLowerCase()
                    + " e não pode ser alterado.");
        }
        if (novoStatus == StatusExame.REALIZADO) {
            throw new RegraNegocioException("Para concluir o atendimento, registre o prontuário.");
        }
        if (novoStatus == exame.getStatus()) {
            return ExameResponse.de(exame);
        }
        exame.setStatus(novoStatus);
        TipoEvento tipo = novoStatus == StatusExame.CANCELADO ? TipoEvento.EXAME_CANCELADO : TipoEvento.EXAME_STATUS_ALTERADO;
        publicar(tipo, exame, exame.getTipo().getDescricao() + " de " + exame.getPet().getNome() + " do dia "
                + exame.getDataHora().format(FORMATO_DATA) + " agora está " + novoStatus.getDescricao().toLowerCase() + ".");
        return ExameResponse.de(exame);
    }

    @Transactional
    public ExameResponse cancelar(Long id) {
        return alterarStatus(id, StatusExame.CANCELADO);
    }

    @Transactional
    public int enviarLembretes() {
        LocalDateTime agora = LocalDateTime.now();
        List<Exame> proximos = exameRepository.findByStatusInAndLembreteEnviadoFalseAndDataHoraBetween(
                STATUS_EM_ABERTO, agora, agora.plusHours(24));
        proximos.forEach(exame -> {
            exame.setLembreteEnviado(true);
            publicar(TipoEvento.EXAME_LEMBRETE, exame, "Lembrete: " + exame.getPet().getNome() + " tem "
                    + exame.getTipo().getDescricao().toLowerCase() + " em " + exame.getDataHora().format(FORMATO_DATA)
                    + " com " + exame.getVeterinario().getNome() + ".");
        });
        return proximos.size();
    }

    Exame encontrar(Long id) {
        return exameRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, id));
    }

    private void publicar(TipoEvento tipo, Exame exame, String descricao) {
        eventoPublisher.publicar(EventoVetPet.doPet(tipo, exame.getPet(), descricao));
    }
}
