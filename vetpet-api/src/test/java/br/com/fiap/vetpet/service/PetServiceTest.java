package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.entity.Pet;
import br.com.fiap.vetpet.domain.entity.Tutor;
import br.com.fiap.vetpet.domain.enums.Especie;
import br.com.fiap.vetpet.dto.request.PetRequest;
import br.com.fiap.vetpet.dto.response.PetResponse;
import br.com.fiap.vetpet.exception.RecursoNaoEncontradoException;
import br.com.fiap.vetpet.exception.RegraNegocioException;
import br.com.fiap.vetpet.messaging.EventoPublisher;
import br.com.fiap.vetpet.repository.PetRepository;
import br.com.fiap.vetpet.repository.TutorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private EventoPublisher eventoPublisher;

    @InjectMocks
    private PetService petService;

    @Test
    void deveVincularTodosOsTutoresInformadosAoCadastrarPet() {
        Tutor mariana = tutor(1L, "Mariana");
        Tutor rafael = tutor(2L, "Rafael");
        when(tutorRepository.findAllById(anySet())).thenReturn(List.of(mariana, rafael));
        when(petRepository.save(any(Pet.class))).thenAnswer(invocacao -> {
            Pet pet = invocacao.getArgument(0);
            pet.setId(10L);
            return pet;
        });

        PetResponse resposta = petService.cadastrar(requisicao(List.of(1L, 2L)));

        assertThat(resposta.getTutores()).hasSize(2);
        assertThat(mariana.getPets()).hasSize(1);
        assertThat(rafael.getPets()).hasSize(1);
    }

    @Test
    void deveFalharQuandoTutorNaoExiste() {
        when(tutorRepository.findAllById(anySet())).thenReturn(List.of(tutor(1L, "Mariana")));

        assertThatThrownBy(() -> petService.cadastrar(requisicao(List.of(1L, 99L))))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void naoDeveRemoverOUnicoTutorDoPet() {
        Tutor mariana = tutor(1L, "Mariana");
        Pet pet = new Pet();
        pet.setId(10L);
        pet.setNome("Luna");
        pet.adicionarTutor(mariana);
        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));

        assertThatThrownBy(() -> petService.desvincularTutor(10L, 1L))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(pet.getTutores()).isEqualTo(Set.of(mariana));
    }

    @Test
    void naoDeveCadastrarPetComMaisDeCincoTutores() {
        assertThatThrownBy(() -> petService.cadastrar(requisicao(List.of(1L, 2L, 3L, 4L, 5L, 6L))))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("no máximo 5 tutores");
    }

    @Test
    void naoDeveVincularSextoTutorAoPet() {
        Pet pet = new Pet();
        pet.setId(10L);
        pet.setNome("Thor");
        for (long id = 1; id <= 5; id++) {
            pet.adicionarTutor(tutor(id, "Tutor" + id));
        }
        Tutor sexto = tutor(6L, "Sexto");
        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(tutorRepository.findById(6L)).thenReturn(Optional.of(sexto));

        assertThatThrownBy(() -> petService.vincularTutor(10L, 6L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("no máximo 5 tutores");
        assertThat(pet.getTutores()).hasSize(5);
    }

    @Test
    void deveCalcularIdadePelaDataDeNascimento() {
        Pet pet = new Pet();
        pet.setDataNascimento(LocalDate.now().minusYears(3).minusDays(1));

        assertThat(pet.calcularIdade()).isEqualTo(3);
    }

    private Tutor tutor(Long id, String nome) {
        Tutor tutor = new Tutor();
        tutor.setId(id);
        tutor.setNome(nome);
        tutor.setEmail(nome.toLowerCase() + "@email.com");
        return tutor;
    }

    private PetRequest requisicao(List<Long> tutores) {
        PetRequest request = new PetRequest();
        request.setNome("Thor");
        request.setEspecie(Especie.CACHORRO);
        request.setDataNascimento(LocalDate.of(2019, 5, 10));
        request.setTutoresIds(tutores);
        return request;
    }
}
