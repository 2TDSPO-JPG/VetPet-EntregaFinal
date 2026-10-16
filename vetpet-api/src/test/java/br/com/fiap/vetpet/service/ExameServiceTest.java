package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.entity.Exame;
import br.com.fiap.vetpet.domain.entity.Pet;
import br.com.fiap.vetpet.domain.entity.Veterinario;
import br.com.fiap.vetpet.domain.enums.StatusExame;
import br.com.fiap.vetpet.domain.enums.TipoAtendimento;
import br.com.fiap.vetpet.domain.enums.TipoEvento;
import br.com.fiap.vetpet.dto.request.ExameRequest;
import br.com.fiap.vetpet.dto.response.ExameResponse;
import br.com.fiap.vetpet.exception.DadoDuplicadoException;
import br.com.fiap.vetpet.exception.RegraNegocioException;
import br.com.fiap.vetpet.messaging.EventoPublisher;
import br.com.fiap.vetpet.messaging.EventoVetPet;
import br.com.fiap.vetpet.repository.ExameRepository;
import br.com.fiap.vetpet.repository.VeterinarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExameServiceTest {

    @Mock
    private ExameRepository exameRepository;

    @Mock
    private VeterinarioRepository veterinarioRepository;

    @Mock
    private PetService petService;

    @Mock
    private EventoPublisher eventoPublisher;

    @InjectMocks
    private ExameService exameService;

    @Test
    void deveAgendarAtendimentoEPublicarEvento() {
        Pet pet = pet();
        Veterinario veterinario = veterinario();
        when(petService.encontrar(1L)).thenReturn(pet);
        when(veterinarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(exameRepository.save(any(Exame.class))).thenAnswer(invocacao -> {
            Exame exame = invocacao.getArgument(0);
            exame.setId(5L);
            return exame;
        });

        ExameResponse resposta = exameService.agendar(requisicao());

        ArgumentCaptor<EventoVetPet> evento = ArgumentCaptor.forClass(EventoVetPet.class);
        verify(eventoPublisher).publicar(evento.capture());
        assertThat(resposta.getStatus()).isEqualTo(StatusExame.AGENDADO);
        assertThat(evento.getValue().tipo()).isEqualTo(TipoEvento.EXAME_AGENDADO);
        assertThat(evento.getValue().routingKey()).isEqualTo("exame.agendado");
    }

    @Test
    void naoDeveAgendarHorarioOcupadoDoVeterinario() {
        when(petService.encontrar(1L)).thenReturn(pet());
        when(veterinarioRepository.findById(2L)).thenReturn(Optional.of(veterinario()));
        when(exameRepository.existsByVeterinarioIdAndDataHoraAndStatusIn(anyLong(), any(), anyCollection()))
                .thenReturn(true);

        assertThatThrownBy(() -> exameService.agendar(requisicao()))
                .isInstanceOf(DadoDuplicadoException.class);
    }

    @Test
    void naoDeveConcluirAtendimentoSemProntuario() {
        when(exameRepository.findById(5L)).thenReturn(Optional.of(exame(StatusExame.AGENDADO)));

        assertThatThrownBy(() -> exameService.alterarStatus(5L, StatusExame.REALIZADO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("prontuário");
    }

    @Test
    void naoDeveAlterarAtendimentoFinalizado() {
        when(exameRepository.findById(5L)).thenReturn(Optional.of(exame(StatusExame.CANCELADO)));

        assertThatThrownBy(() -> exameService.cancelar(5L))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveCancelarAtendimentoEmAbertoEPublicarCancelamento() {
        Exame exame = exame(StatusExame.AGENDADO);
        when(exameRepository.findById(5L)).thenReturn(Optional.of(exame));

        exameService.cancelar(5L);

        assertThat(exame.getStatus()).isEqualTo(StatusExame.CANCELADO);
        verify(eventoPublisher).publicar(argThat(evento -> evento.tipo() == TipoEvento.EXAME_CANCELADO
                && evento.petId().equals(exame.getPet().getId())));
    }

    private ExameRequest requisicao() {
        ExameRequest request = new ExameRequest();
        request.setPetId(1L);
        request.setVeterinarioId(2L);
        request.setTipo(TipoAtendimento.CONSULTA);
        request.setDataHora(LocalDateTime.now().plusDays(3).withSecond(0).withNano(0));
        return request;
    }

    private Exame exame(StatusExame status) {
        Exame exame = new Exame();
        exame.setId(5L);
        exame.setPet(pet());
        exame.setVeterinario(veterinario());
        exame.setTipo(TipoAtendimento.CONSULTA);
        exame.setDataHora(LocalDateTime.now().plusDays(1));
        exame.setStatus(status);
        return exame;
    }

    private Pet pet() {
        Pet pet = new Pet();
        pet.setId(1L);
        pet.setNome("Thor");
        return pet;
    }

    private Veterinario veterinario() {
        Veterinario veterinario = new Veterinario();
        veterinario.setId(2L);
        veterinario.setNome("Ana Ribeiro");
        return veterinario;
    }
}
