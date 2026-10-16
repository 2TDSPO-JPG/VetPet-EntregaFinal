package br.com.fiap.vetpet.notificacao;

import br.com.fiap.vetpet.notificacao.domain.Canal;
import br.com.fiap.vetpet.notificacao.domain.Notificacao;
import br.com.fiap.vetpet.notificacao.messaging.EventoVetPet;
import br.com.fiap.vetpet.notificacao.repository.NotificacaoRepository;
import br.com.fiap.vetpet.notificacao.service.EmailService;
import br.com.fiap.vetpet.notificacao.service.NotificacaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificacaoService notificacaoService;

    @Test
    void deveRegistrarNotificacaoPorEmailParaOsTutores() {
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        when(emailService.enviar(any(), anyList())).thenReturn(Canal.EMAIL);

        Optional<Notificacao> resultado = notificacaoService.processar(evento("evt-1", "EXAME_AGENDADO"));

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCanal()).isEqualTo(Canal.EMAIL);
        assertThat(resultado.get().getDestinatarios()).containsExactly("mariana@email.com", "rafael@email.com");
    }

    @Test
    void perguntaNovaVaiParaOPainelDaClinica() {
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Optional<Notificacao> resultado = notificacaoService.processar(evento("evt-2", "PERGUNTA_CRIADA"));

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCanal()).isEqualTo(Canal.PAINEL_CLINICA);
        assertThat(resultado.get().getDestinatarios()).isEmpty();
        verify(emailService, never()).enviar(any(), anyList());
    }

    @Test
    void naoDeveProcessarOMesmoEventoDuasVezes() {
        when(notificacaoRepository.existsByEventoId("evt-3")).thenReturn(true);

        Optional<Notificacao> resultado = notificacaoService.processar(evento("evt-3", "EXAME_AGENDADO"));

        assertThat(resultado).isEmpty();
        verify(notificacaoRepository, never()).save(any());
        verify(emailService, never()).enviar(any(), anyList());
    }

    private EventoVetPet evento(String id, String tipo) {
        return new EventoVetPet(id, tipo, 1L, "Thor", List.of("mariana@email.com", "rafael@email.com"),
                "Atendimento agendado", "Consulta de Thor amanhã às 10:00.", LocalDateTime.now());
    }
}
