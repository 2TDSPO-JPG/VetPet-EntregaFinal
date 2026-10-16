package br.com.fiap.vetpet.dto.request;

import br.com.fiap.vetpet.domain.enums.StatusExame;
import jakarta.validation.constraints.NotNull;

public record StatusExameRequest(@NotNull(message = "Informe o status") StatusExame status) {
}
