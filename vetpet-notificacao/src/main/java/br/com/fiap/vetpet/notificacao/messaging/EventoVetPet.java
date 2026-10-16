package br.com.fiap.vetpet.notificacao.messaging;

import java.time.LocalDateTime;
import java.util.List;

public record EventoVetPet(
        String id,
        String tipo,
        Long petId,
        String petNome,
        List<String> destinatarios,
        String titulo,
        String descricao,
        LocalDateTime ocorridoEm) {
}
