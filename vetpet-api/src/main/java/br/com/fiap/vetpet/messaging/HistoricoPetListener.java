package br.com.fiap.vetpet.messaging;

import br.com.fiap.vetpet.service.HistoricoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HistoricoPetListener {

    private final HistoricoService historicoService;

    @RabbitListener(queues = "${vetpet.mensageria.fila-historico}")
    public void receber(EventoVetPet evento) {
        log.info("Evento {} ({}) recebido da fila de histórico", evento.id(), evento.tipo());
        historicoService.registrar(evento);
    }
}
