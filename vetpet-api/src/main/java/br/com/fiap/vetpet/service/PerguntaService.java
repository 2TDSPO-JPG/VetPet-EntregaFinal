package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.entity.Pergunta;
import br.com.fiap.vetpet.domain.entity.Pet;
import br.com.fiap.vetpet.domain.enums.TipoEvento;
import br.com.fiap.vetpet.dto.request.PerguntaRequest;
import br.com.fiap.vetpet.dto.request.RespostaRequest;
import br.com.fiap.vetpet.dto.response.PerguntaResponse;
import br.com.fiap.vetpet.exception.RecursoNaoEncontradoException;
import br.com.fiap.vetpet.messaging.EventoPublisher;
import br.com.fiap.vetpet.messaging.EventoVetPet;
import br.com.fiap.vetpet.repository.PerguntaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerguntaService {

    private static final String RECURSO = "Pergunta";

    private final PerguntaRepository perguntaRepository;
    private final PetService petService;
    private final EventoPublisher eventoPublisher;

    public List<PerguntaResponse> listarDoPet(Long petId) {
        return perguntaRepository.findByPetIdOrderByCriadaEmDesc(petId).stream().map(PerguntaResponse::de).toList();
    }

    public List<PerguntaResponse> listarPendentes() {
        return perguntaRepository.findTop5ByRespostaIsNullOrderByCriadaEmAsc().stream()
                .map(PerguntaResponse::de).toList();
    }

    @Transactional
    public PerguntaResponse criar(Long petId, PerguntaRequest request, String autor) {
        Pet pet = petService.encontrar(petId);
        Pergunta pergunta = new Pergunta();
        pergunta.setPet(pet);
        pergunta.setTexto(request.getTexto().trim());
        pergunta.setAutor(autor);
        Pergunta salva = perguntaRepository.save(pergunta);
        eventoPublisher.publicar(EventoVetPet.doPet(TipoEvento.PERGUNTA_CRIADA, pet,
                "Nova dúvida sobre " + pet.getNome() + ": \"" + salva.getTexto() + "\""));
        return PerguntaResponse.de(salva);
    }

    @Transactional
    public PerguntaResponse responder(Long id, RespostaRequest request, String respondidaPor) {
        Pergunta pergunta = encontrar(id);
        pergunta.setResposta(request.getResposta().trim());
        pergunta.setRespondidaPor(respondidaPor);
        pergunta.setRespondidaEm(LocalDateTime.now());
        eventoPublisher.publicar(EventoVetPet.doPet(TipoEvento.PERGUNTA_RESPONDIDA, pergunta.getPet(),
                "A equipe respondeu sua dúvida sobre " + pergunta.getPet().getNome() + ": " + pergunta.getResposta()));
        return PerguntaResponse.de(pergunta);
    }

    @Transactional
    public Long excluir(Long id) {
        Pergunta pergunta = encontrar(id);
        Long petId = pergunta.getPet().getId();
        pergunta.getPet().getPerguntas().remove(pergunta);
        perguntaRepository.delete(pergunta);
        return petId;
    }

    public PerguntaResponse buscar(Long id) {
        return PerguntaResponse.de(encontrar(id));
    }

    private Pergunta encontrar(Long id) {
        return perguntaRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, id));
    }
}
