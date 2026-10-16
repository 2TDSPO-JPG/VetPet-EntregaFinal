package br.com.fiap.vetpet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class VetPetApplication {

    public static void main(String[] args) {
        SpringApplication.run(VetPetApplication.class, args);
    }
}
