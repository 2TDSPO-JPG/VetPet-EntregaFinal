package br.com.fiap.vetpet.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "VetPet API",
                version = "1.0.0",
                description = "API de acompanhamento contínuo da saúde de pets: tutores, pets, veterinários, "
                        + "atendimentos, prontuários, perguntas e linha do tempo alimentada por eventos RabbitMQ.",
                contact = @Contact(name = "Equipe VetPet - FIAP")),
        security = @SecurityRequirement(name = OpenApiConfig.BASIC_AUTH))
@SecurityScheme(name = OpenApiConfig.BASIC_AUTH, type = SecuritySchemeType.HTTP, scheme = "basic")
public class OpenApiConfig {

    public static final String BASIC_AUTH = "basicAuth";
}
