package br.com.fiap.vetpet.messaging;

import br.com.fiap.vetpet.domain.entity.Pet;
import br.com.fiap.vetpet.domain.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventoVetPet(
        String id,
        TipoEvento tipo,
        Long petId,
        String petNome,
        List<String> destinatarios,
        String titulo,
        String descricao,
        LocalDateTime ocorridoEm) {

    public static EventoVetPet doPet(TipoEvento tipo, Pet pet, String descricao) {
        return new EventoVetPet(UUID.randomUUID().toString(), tipo, pet.getId(), pet.getNome(),
                pet.emailsTutores(), tipo.getDescricao(), descricao, LocalDateTime.now());
    }

    public static EventoVetPet semPet(TipoEvento tipo, List<String> destinatarios, String descricao) {
        return new EventoVetPet(UUID.randomUUID().toString(), tipo, null, null,
                destinatarios, tipo.getDescricao(), descricao, LocalDateTime.now());
    }

    public String routingKey() {
        return tipo.getRoutingKey();
    }
}
