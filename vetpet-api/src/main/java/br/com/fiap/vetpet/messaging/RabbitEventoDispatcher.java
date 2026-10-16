package br.com.fiap.vetpet.messaging;

import br.com.fiap.vetpet.service.HistoricoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitEventoDispatcher {

    private final RabbitTemplate rabbitTemplate;
    private final MensageriaProperties properties;
    private final HistoricoService historicoService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void despachar(EventoVetPet evento) {
        if (!properties.habilitada()) {
            historicoService.registrar(evento);
            return;
        }
        try {
            rabbitTemplate.convertAndSend(properties.exchange(), evento.routingKey(), evento);
            log.info("Evento {} publicado no RabbitMQ com routing key {}", evento.id(), evento.routingKey());
        } catch (AmqpException e) {
            log.warn("RabbitMQ indisponível, registrando evento {} diretamente no histórico: {}",
                    evento.id(), e.getMessage());
            historicoService.registrar(evento);
        }
    }
}
