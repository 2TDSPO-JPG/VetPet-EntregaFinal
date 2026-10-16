package br.com.fiap.vetpet.dto.request;

import br.com.fiap.vetpet.domain.entity.Prontuario;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ProntuarioRequest {

    @NotNull(message = "Informe o atendimento")
    private Long exameId;

    @NotBlank(message = "Informe o diagnóstico")
    @Size(max = 2000)
    private String diagnostico;

    @Size(max = 2000)
    private String prescricao;

    @Size(max = 1000)
    private String examesSolicitados;

    @Size(max = 2000)
    private String observacoes;

    @DecimalMin(value = "0.01", message = "O peso deve ser maior que zero")
    @DecimalMax(value = "2000.00", message = "Peso inválido")
    private BigDecimal pesoAferidoKg;

    @FutureOrPresent(message = "O retorno não pode estar no passado")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate proximoRetorno;

    public static ProntuarioRequest de(Prontuario prontuario) {
        ProntuarioRequest request = new ProntuarioRequest();
        request.setExameId(prontuario.getExame().getId());
        request.setDiagnostico(prontuario.getDiagnostico());
        request.setPrescricao(prontuario.getPrescricao());
        request.setExamesSolicitados(prontuario.getExamesSolicitados());
        request.setObservacoes(prontuario.getObservacoes());
        request.setPesoAferidoKg(prontuario.getPesoAferidoKg());
        request.setProximoRetorno(prontuario.getProximoRetorno());
        return request;
    }
}
