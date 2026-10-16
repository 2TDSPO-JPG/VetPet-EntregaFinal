package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.entity.Exame;
import br.com.fiap.vetpet.domain.entity.Pet;
import br.com.fiap.vetpet.domain.entity.Prontuario;
import br.com.fiap.vetpet.domain.entity.Veterinario;
import br.com.fiap.vetpet.domain.enums.StatusExame;
import br.com.fiap.vetpet.domain.enums.TipoAtendimento;
import br.com.fiap.vetpet.dto.request.ProntuarioRequest;
import br.com.fiap.vetpet.exception.RegraNegocioException;
import br.com.fiap.vetpet.messaging.EventoPublisher;
import br.com.fiap.vetpet.repository.ProntuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProntuarioServiceTest {

    @Mock
    private ProntuarioRepository prontuarioRepository;

    @Mock
    private ExameService exameService;

    @Mock
    private EventoPublisher eventoPublisher;

    @InjectMocks
    private ProntuarioService prontuarioService;

    @Test
    void deveConcluirAtendimentoEAtualizarPesoDoPet() {
        Exame exame = exame(StatusExame.AGENDADO);
        when(exameService.encontrar(5L)).thenReturn(exame);
        when(prontuarioRepository.save(any(Prontuario.class))).thenAnswer(invocacao -> {
            Prontuario prontuario = invocacao.getArgument(0);
            prontuario.setId(7L);
            return prontuario;
        });

        prontuarioService.registrar(requisicao());

        assertThat(exame.getStatus()).isEqualTo(StatusExame.REALIZADO);
        assertThat(exame.getPet().getPesoKg()).isEqualByComparingTo("31.20");
        verify(eventoPublisher).publicar(any());
    }

    @Test
    void naoDeveRegistrarProntuarioDeAtendimentoCancelado() {
        when(exameService.encontrar(5L)).thenReturn(exame(StatusExame.CANCELADO));

        assertThatThrownBy(() -> prontuarioService.registrar(requisicao()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("cancelado");
        verify(prontuarioRepository, never()).save(any());
    }

    @Test
    void naoDeveRegistrarDoisProntuariosParaOMesmoAtendimento() {
        when(exameService.encontrar(5L)).thenReturn(exame(StatusExame.REALIZADO));
        when(prontuarioRepository.existsByExameId(5L)).thenReturn(true);

        assertThatThrownBy(() -> prontuarioService.registrar(requisicao()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já possui");
    }

    private ProntuarioRequest requisicao() {
        ProntuarioRequest request = new ProntuarioRequest();
        request.setExameId(5L);
        request.setDiagnostico("Saudável");
        request.setPesoAferidoKg(new BigDecimal("31.20"));
        return request;
    }

    private Exame exame(StatusExame status) {
        Pet pet = new Pet();
        pet.setId(1L);
        pet.setNome("Thor");
        pet.setPesoKg(new BigDecimal("32.50"));
        Veterinario veterinario = new Veterinario();
        veterinario.setId(2L);
        veterinario.setNome("Ana Ribeiro");
        Exame exame = new Exame();
        exame.setId(5L);
        exame.setPet(pet);
        exame.setVeterinario(veterinario);
        exame.setTipo(TipoAtendimento.CONSULTA);
        exame.setDataHora(LocalDateTime.now());
        exame.setStatus(status);
        return exame;
    }
}
