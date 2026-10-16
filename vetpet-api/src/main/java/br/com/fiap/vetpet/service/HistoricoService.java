package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.entity.EventoPet;
import br.com.fiap.vetpet.dto.response.EventoPetResponse;
import br.com.fiap.vetpet.messaging.EventoVetPet;
import br.com.fiap.vetpet.repository.EventoPetRepository;
import br.com.fiap.vetpet.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final EventoPetRepository eventoPetRepository;
    private final PetRepository petRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(EventoVetPet evento) {
        if (evento.petId() == null || eventoPetRepository.existsByEventoId(evento.id())) {
            return;
        }
        petRepository.findById(evento.petId()).ifPresent(pet -> {
            EventoPet registro = new EventoPet();
            registro.setEventoId(evento.id());
            registro.setPet(pet);
            registro.setTipo(evento.tipo());
            registro.setTitulo(evento.titulo());
            registro.setDescricao(limitar(evento.descricao()));
            registro.setOcorridoEm(evento.ocorridoEm());
            eventoPetRepository.save(registro);
        });
    }

    @Transactional(readOnly = true)
    public List<EventoPetResponse> linhaDoTempo(Long petId) {
        return eventoPetRepository.findByPetIdOrderByOcorridoEmDesc(petId).stream()
                .map(EventoPetResponse::de)
                .toList();
    }

    private String limitar(String texto) {
        if (texto == null || texto.length() <= 1000) {
            return texto;
        }
        return texto.substring(0, 997) + "...";
    }
}
