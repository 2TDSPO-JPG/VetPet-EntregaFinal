package br.com.fiap.vetpet.config;

import br.com.fiap.vetpet.messaging.MensageriaProperties;
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

    private static final List<String> ROTAS_HISTORICO = List.of("pet.#", "exame.#", "prontuario.#", "pergunta.#");

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public TopicExchange eventosExchange(MensageriaProperties properties) {
        return new TopicExchange(properties.exchange(), true, false);
    }

    @Bean
    public DirectExchange eventosDlxExchange(MensageriaProperties properties) {
        return new DirectExchange(properties.exchangeDlx(), true, false);
    }

    @Bean
    public Queue filaHistorico(MensageriaProperties properties) {
        return QueueBuilder.durable(properties.filaHistorico())
                .deadLetterExchange(properties.exchangeDlx())
                .deadLetterRoutingKey(properties.filaHistorico())
                .build();
    }

    @Bean
    public Queue filaHistoricoDlq(MensageriaProperties properties) {
        return QueueBuilder.durable(properties.filaHistoricoDlq()).build();
    }

    @Bean
    public Declarables bindingsHistorico(Queue filaHistorico, Queue filaHistoricoDlq,
                                         TopicExchange eventosExchange, DirectExchange eventosDlxExchange,
                                         MensageriaProperties properties) {
        Stream<Binding> rotas = ROTAS_HISTORICO.stream()
                .map(rota -> BindingBuilder.bind(filaHistorico).to(eventosExchange).with(rota));
        Binding dlq = BindingBuilder.bind(filaHistoricoDlq).to(eventosDlxExchange).with(properties.filaHistorico());
        return new Declarables(Stream.concat(rotas, Stream.of(dlq)).toArray(Declarable[]::new));
    }
}
