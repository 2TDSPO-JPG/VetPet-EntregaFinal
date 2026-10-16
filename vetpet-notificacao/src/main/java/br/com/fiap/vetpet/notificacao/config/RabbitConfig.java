package br.com.fiap.vetpet.notificacao.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Stream;

@Configuration
public class RabbitConfig {

    private static final List<String> ROTAS = List.of("tutor.cadastrado", "pet.#", "exame.#", "prontuario.#",
            "pergunta.#");

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public TopicExchange eventosExchange(NotificacaoProperties properties) {
        return new TopicExchange(properties.exchange(), true, false);
    }

    @Bean
    public DirectExchange eventosDlxExchange(NotificacaoProperties properties) {
        return new DirectExchange(properties.exchangeDlx(), true, false);
    }

    @Bean
    public Queue filaNotificacoes(NotificacaoProperties properties) {
        return QueueBuilder.durable(properties.fila())
                .deadLetterExchange(properties.exchangeDlx())
                .deadLetterRoutingKey(properties.fila())
                .build();
    }

    @Bean
    public Queue filaNotificacoesDlq(NotificacaoProperties properties) {
        return QueueBuilder.durable(properties.filaDlq()).build();
    }

    @Bean
    public Declarables bindingsNotificacoes(Queue filaNotificacoes, Queue filaNotificacoesDlq,
                                            TopicExchange eventosExchange, DirectExchange eventosDlxExchange,
                                            NotificacaoProperties properties) {
        Stream<Binding> rotas = ROTAS.stream()
                .map(rota -> BindingBuilder.bind(filaNotificacoes).to(eventosExchange).with(rota));
        Binding dlq = BindingBuilder.bind(filaNotificacoesDlq).to(eventosDlxExchange).with(properties.fila());
        return new Declarables(Stream.concat(rotas, Stream.of(dlq)).toArray(Declarable[]::new));
    }
}
