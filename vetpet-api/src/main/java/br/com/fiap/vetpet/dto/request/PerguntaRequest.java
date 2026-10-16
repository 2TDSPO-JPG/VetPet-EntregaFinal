package br.com.fiap.vetpet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PerguntaRequest {

    @NotBlank(message = "Escreva a pergunta")
    @Size(max = 1000, message = "A pergunta deve ter no máximo 1000 caracteres")
    private String texto;
}
