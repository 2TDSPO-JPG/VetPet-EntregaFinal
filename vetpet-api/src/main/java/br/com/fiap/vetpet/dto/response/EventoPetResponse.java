package br.com.fiap.vetpet.dto.response;

import br.com.fiap.vetpet.domain.entity.EventoPet;
import br.com.fiap.vetpet.domain.enums.TipoEvento;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EventoPetResponse {

    private TipoEvento tipo;
    private String titulo;
    private String descricao;
    private LocalDateTime ocorridoEm;

    public static EventoPetResponse de(EventoPet evento) {
        return new EventoPetResponse(evento.getTipo(), evento.getTitulo(), evento.getDescricao(),
                evento.getOcorridoEm());
    }

    public String getCategoria() {
        return tipo.getRoutingKey().substring(0, tipo.getRoutingKey().indexOf('.'));
    }
}
