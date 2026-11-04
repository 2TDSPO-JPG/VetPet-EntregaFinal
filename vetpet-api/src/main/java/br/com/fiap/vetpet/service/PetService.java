package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.entity.Pet;
import br.com.fiap.vetpet.domain.entity.Tutor;
import br.com.fiap.vetpet.domain.enums.TipoEvento;
import br.com.fiap.vetpet.dto.request.PetRequest;
import br.com.fiap.vetpet.dto.response.PetResponse;
import br.com.fiap.vetpet.dto.response.PetResumoResponse;
import br.com.fiap.vetpet.exception.AcessoNegadoException;
import br.com.fiap.vetpet.exception.RecursoNaoEncontradoException;
import br.com.fiap.vetpet.exception.RegraNegocioException;
import br.com.fiap.vetpet.messaging.EventoPublisher;
import br.com.fiap.vetpet.messaging.EventoVetPet;
import br.com.fiap.vetpet.repository.PetRepository;
import br.com.fiap.vetpet.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private static final String RECURSO = "Pet";
    static final int LIMITE_TUTORES = 5;

    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;
    private final EventoPublisher eventoPublisher;

    public Page<PetResponse> listar(String termo, Pageable pageable) {
        return petRepository.buscar(termo == null ? "" : termo.trim(), pageable).map(PetResponse::de);
    }

    public List<PetResumoResponse> listarParaSelecao() {
        return petRepository.findAll(Sort.by("nome")).stream().map(PetResumoResponse::de).toList();
    }

    public PetResponse buscar(Long id) {
        return PetResponse.de(encontrar(id));
    }

    public PetResponse buscarPorCodigo(String petCode) {
        return petRepository.findByPetCodeIgnoreCase(petCode.trim())
                .map(PetResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, petCode));
    }

    public PetRequest buscarParaEdicao(Long id) {
        return PetRequest.de(encontrar(id));
    }

    public List<PetResponse> listarDoTutor(String email) {
        return petRepository.findByTutorEmail(email).stream().map(PetResponse::de).toList();
    }

    public void garantirAcessoDoTutor(Long petId, String email) {
        if (!petRepository.existsByIdAndTutoresEmailIgnoreCase(petId, email)) {
            throw new AcessoNegadoException("Este pet não está vinculado à sua conta.");
        }
    }

    @Transactional
    public PetResponse cadastrar(PetRequest request) {
        Pet pet = new Pet();
        aplicar(request, pet);
        carregarTutores(request.getTutoresIds()).forEach(pet::adicionarTutor);
        Pet salvo = petRepository.save(pet);
        eventoPublisher.publicar(EventoVetPet.doPet(TipoEvento.PET_CADASTRADO, salvo,
                salvo.getNome() + " (" + salvo.getEspecie().getDescricao() + ") entrou para o acompanhamento VetPet com o código "
                        + salvo.getPetCode() + "."));
        return PetResponse.de(salvo);
    }

    @Transactional
    public PetResponse atualizar(Long id, PetRequest request) {
        Pet pet = encontrar(id);
        aplicar(request, pet);
        Set<Tutor> novos = carregarTutores(request.getTutoresIds());
        List.copyOf(pet.getTutores()).stream()
                .filter(tutor -> !novos.contains(tutor))
                .forEach(pet::removerTutor);
        novos.stream()
                .filter(tutor -> !pet.getTutores().contains(tutor))
                .forEach(tutor -> {
                    pet.adicionarTutor(tutor);
                    publicarVinculo(pet, tutor);
                });
        eventoPublisher.publicar(EventoVetPet.doPet(TipoEvento.PET_ATUALIZADO, pet,
                "Cadastro de " + pet.getNome() + " atualizado."));
        return PetResponse.de(pet);
    }

    @Transactional
    public PetResponse vincularTutor(Long petId, Long tutorId) {
        Pet pet = encontrar(petId);
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tutor", tutorId));
        if (pet.getTutores().contains(tutor)) {
            throw new RegraNegocioException(tutor.getNome() + " já é tutor(a) de " + pet.getNome() + ".");
        }
        validarLimiteDeTutores(pet.getTutores().size() + 1);
        pet.adicionarTutor(tutor);
        publicarVinculo(pet, tutor);
        return PetResponse.de(pet);
    }

    @Transactional
    public PetResponse desvincularTutor(Long petId, Long tutorId) {
        Pet pet = encontrar(petId);
        Tutor tutor = pet.getTutores().stream()
                .filter(t -> t.getId().equals(tutorId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vínculo com o tutor", tutorId));
        if (pet.getTutores().size() == 1) {
            throw new RegraNegocioException("O pet precisa ter pelo menos um tutor responsável.");
        }
        pet.removerTutor(tutor);
        return PetResponse.de(pet);
    }

    @Transactional
    public void excluir(Long id) {
        Pet pet = encontrar(id);
        List.copyOf(pet.getTutores()).forEach(pet::removerTutor);
        petRepository.delete(pet);
    }

    Pet encontrar(Long id) {
        return petRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, id));
    }

    private void aplicar(PetRequest request, Pet pet) {
        pet.setNome(request.getNome().trim());
        pet.setDataNascimento(request.getDataNascimento());
        pet.setEspecie(request.getEspecie());
        pet.setRaca(DadosPessoais.vazioParaNulo(request.getRaca()));
        pet.setSexo(request.getSexo());
        pet.setPesoKg(request.getPesoKg());
        pet.setAlergias(DadosPessoais.vazioParaNulo(request.getAlergias()));
    }

    private void validarLimiteDeTutores(int quantidade) {
        if (quantidade > LIMITE_TUTORES) {
            throw new RegraNegocioException("Um pet pode ter no máximo " + LIMITE_TUTORES + " tutores.");
        }
    }

    private Set<Tutor> carregarTutores(List<Long> ids) {
        Set<Long> idsUnicos = new HashSet<>(ids);
        validarLimiteDeTutores(idsUnicos.size());
        List<Tutor> tutores = tutorRepository.findAllById(idsUnicos);
        if (tutores.size() != idsUnicos.size()) {
            Set<Long> encontrados = new HashSet<>(tutores.stream().map(Tutor::getId).toList());
            idsUnicos.removeAll(encontrados);
            throw new RecursoNaoEncontradoException("Tutor", idsUnicos);
        }
        return new LinkedHashSet<>(tutores);
    }

    private void publicarVinculo(Pet pet, Tutor tutor) {
        eventoPublisher.publicar(EventoVetPet.doPet(TipoEvento.TUTOR_VINCULADO, pet,
                tutor.getNome() + " agora também acompanha " + pet.getNome() + "."));
    }
}
