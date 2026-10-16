package br.com.fiap.vetpet.notificacao.messaging;

import br.com.fiap.vetpet.notificacao.service.NotificacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoListener {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacaoListener.class);

    private final NotificacaoService notificacaoService;

    public NotificacaoListener(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @RabbitListener(queues = "${notificacao.fila}")
    public void receber(EventoVetPet evento) {
        LOG.info("Evento {} ({}) recebido para notificação", evento.id(), evento.tipo());
        notificacaoService.processar(evento);
    }
}
