package br.com.fiap.vetpet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RespostaRequest {

    @NotBlank(message = "Escreva a resposta")
    @Size(max = 2000, message = "A resposta deve ter no máximo 2000 caracteres")
    private String resposta;
}
