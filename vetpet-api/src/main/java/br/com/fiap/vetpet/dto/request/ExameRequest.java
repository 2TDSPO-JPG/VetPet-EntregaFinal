package br.com.fiap.vetpet.dto.request;

import br.com.fiap.vetpet.domain.enums.TipoAtendimento;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ExameRequest {

    @NotNull(message = "Selecione o pet")
    private Long petId;

    @NotNull(message = "Selecione o veterinário")
    private Long veterinarioId;

    @NotNull(message = "Selecione o tipo de atendimento")
    private TipoAtendimento tipo;

    @NotNull(message = "Informe a data e o horário")
    @Future(message = "O atendimento deve ser agendado para uma data futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dataHora;

    @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres")
    private String motivo;
}
