package br.com.fiap.vetpet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaRequest(
        @NotBlank(message = "Informe a senha atual") String senhaAtual,
        @NotBlank(message = "Informe a nova senha")
        @Size(min = 6, max = 60, message = "A nova senha deve ter entre 6 e 60 caracteres") String novaSenha) {
}
