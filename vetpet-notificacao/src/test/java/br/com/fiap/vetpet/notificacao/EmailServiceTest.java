package br.com.fiap.vetpet.notificacao;

import br.com.fiap.vetpet.notificacao.config.EmailProperties;
import br.com.fiap.vetpet.notificacao.domain.Canal;
import br.com.fiap.vetpet.notificacao.messaging.EventoVetPet;
import br.com.fiap.vetpet.notificacao.service.EmailService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private ObjectProvider<JavaMailSender> provider;

    @Mock
    private JavaMailSender mailSender;

    @Test
    void semRemetenteConfiguradoOEnvioESimulado() {
        when(provider.getIfAvailable()).thenReturn(mailSender);
        EmailService service = new EmailService(provider, new EmailProperties("", List.of("example.com")));

        Canal canal = service.enviar(evento(), List.of("pedro@gmail.com"));

        assertThat(canal).isEqualTo(Canal.EMAIL_SIMULADO);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void enviaSomenteParaDominiosReais() {
        when(provider.getIfAvailable()).thenReturn(mailSender);
        when(mailSender.createMimeMessage()).thenAnswer(invocacao -> new MimeMessage(Session.getInstance(new Properties())));
        EmailService service = new EmailService(provider, new EmailProperties("clinica@gmail.com", List.of("example.com")));

        Canal canal = service.enviar(evento(), List.of("pedro@gmail.com", "mariana@example.com"));

        assertThat(canal).isEqualTo(Canal.EMAIL);
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void falhaNoSmtpNaoInterrompeOProcessamento() {
        when(provider.getIfAvailable()).thenReturn(mailSender);
        when(mailSender.createMimeMessage()).thenAnswer(invocacao -> new MimeMessage(Session.getInstance(new Properties())));
        doThrow(new MailSendException("SMTP indisponível")).when(mailSender).send(any(MimeMessage.class));
        EmailService service = new EmailService(provider, new EmailProperties("clinica@gmail.com", List.of()));

        Canal canal = service.enviar(evento(), List.of("pedro@gmail.com"));

        assertThat(canal).isEqualTo(Canal.EMAIL_FALHOU);
    }

    private EventoVetPet evento() {
        return new EventoVetPet("evt-email", "EXAME_AGENDADO", 1L, "Thor", List.of(), "Atendimento agendado",
                "Consulta de Thor amanhã às 10:00.", LocalDateTime.now());
    }
}
