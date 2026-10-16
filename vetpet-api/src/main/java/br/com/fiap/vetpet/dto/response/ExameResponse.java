package br.com.fiap.vetpet.dto.response;

import br.com.fiap.vetpet.domain.entity.Exame;
import br.com.fiap.vetpet.domain.enums.StatusExame;
import br.com.fiap.vetpet.domain.enums.TipoAtendimento;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ExameResponse {

    private Long id;
    private LocalDateTime dataHora;
    private TipoAtendimento tipo;
    private StatusExame status;
    private String motivo;
    private Long petId;
    private String petNome;
    private String petCode;
    private Long veterinarioId;
    private String veterinarioNome;
    private Long prontuarioId;

    public static ExameResponse de(Exame exame) {
        return ExameResponse.builder()
                .id(exame.getId())
                .dataHora(exame.getDataHora())
                .tipo(exame.getTipo())
                .status(exame.getStatus())
                .motivo(exame.getMotivo())
                .petId(exame.getPet().getId())
                .petNome(exame.getPet().getNome())
                .petCode(exame.getPet().getPetCode())
                .veterinarioId(exame.getVeterinario().getId())
                .veterinarioNome(exame.getVeterinario().getNome())
                .prontuarioId(exame.getProntuario() != null ? exame.getProntuario().getId() : null)
                .build();
    }

    public boolean isPodeRegistrarProntuario() {
        return prontuarioId == null && (status == StatusExame.AGENDADO || status == StatusExame.ADIADO);
    }

    public boolean isPodeCancelar() {
        return status == StatusExame.AGENDADO || status == StatusExame.ADIADO;
    }
}
