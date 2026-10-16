package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.entity.Tutor;
import br.com.fiap.vetpet.dto.request.TutorRequest;
import br.com.fiap.vetpet.dto.response.TutorResponse;
import br.com.fiap.vetpet.exception.DadoDuplicadoException;
import br.com.fiap.vetpet.messaging.EventoPublisher;
import br.com.fiap.vetpet.messaging.EventoVetPet;
import br.com.fiap.vetpet.repository.TutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TutorServiceTest {

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private EventoPublisher eventoPublisher;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private TutorService tutorService;

    @BeforeEach
    void configurar() {
        tutorService = new TutorService(tutorRepository, passwordEncoder, eventoPublisher);
    }

    @Test
    void deveCadastrarTutorComSenhaCriptografadaECpfFormatado() {
        when(tutorRepository.save(any(Tutor.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        TutorResponse resposta = tutorService.cadastrar(requisicao("MARIANA@Email.com", "12345678909"));

        ArgumentCaptor<Tutor> salvo = ArgumentCaptor.forClass(Tutor.class);
        verify(tutorRepository).save(salvo.capture());
        assertThat(resposta.getEmail()).isEqualTo("mariana@email.com");
        assertThat(resposta.getCpf()).isEqualTo("123.456.789-09");
        assertThat(salvo.getValue().getCredencial().getSenhaHash()).isNotEqualTo("segredo1");
        assertThat(passwordEncoder.matches("segredo1", salvo.getValue().getCredencial().getSenhaHash())).isTrue();
        verify(eventoPublisher).publicar(any(EventoVetPet.class));
    }

    @Test
    void naoDeveCadastrarEmailDuplicado() {
        when(tutorRepository.existsByEmailIgnoreCase("mariana@email.com")).thenReturn(true);

        assertThatThrownBy(() -> tutorService.cadastrar(requisicao("mariana@email.com", "12345678909")))
                .isInstanceOf(DadoDuplicadoException.class)
                .hasMessageContaining("e-mail");
        verify(tutorRepository, never()).save(any());
    }

    @Test
    void naoDeveCadastrarCpfDuplicado() {
        when(tutorRepository.existsByCpf("123.456.789-09")).thenReturn(true);

        assertThatThrownBy(() -> tutorService.cadastrar(requisicao("nova@email.com", "123.456.789-09")))
                .isInstanceOf(DadoDuplicadoException.class)
                .hasMessageContaining("CPF");
    }

    private TutorRequest requisicao(String email, String cpf) {
        TutorRequest request = new TutorRequest();
        request.setNome("Mariana Costa");
        request.setEmail(email);
        request.setCpf(cpf);
        request.setSenha("segredo1");
        return request;
    }
}
