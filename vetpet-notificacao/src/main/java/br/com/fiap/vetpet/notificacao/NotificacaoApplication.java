package br.com.fiap.vetpet.notificacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NotificacaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacaoApplication.class, args);
    }
}
