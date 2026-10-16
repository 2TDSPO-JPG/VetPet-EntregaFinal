package br.com.fiap.vetpet.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventoPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publicar(EventoVetPet evento) {
        applicationEventPublisher.publishEvent(evento);
    }
}
