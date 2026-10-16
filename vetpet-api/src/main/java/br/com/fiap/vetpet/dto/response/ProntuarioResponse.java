package br.com.fiap.vetpet.dto.response;

import br.com.fiap.vetpet.domain.entity.Exame;
import br.com.fiap.vetpet.domain.entity.Prontuario;
import br.com.fiap.vetpet.domain.enums.TipoAtendimento;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProntuarioResponse {

    private Long id;
    private Long exameId;
    private LocalDateTime dataAtendimento;
    private TipoAtendimento tipoAtendimento;
    private Long petId;
    private String petNome;
    private String petCode;
    private Long veterinarioId;
    private String veterinarioNome;
    private String diagnostico;
    private String prescricao;
    private String examesSolicitados;
    private String observacoes;
    private BigDecimal pesoAferidoKg;
    private LocalDate proximoRetorno;
    private LocalDateTime registradoEm;
    private LocalDateTime atualizadoEm;

    public static ProntuarioResponse de(Prontuario prontuario) {
        Exame exame = prontuario.getExame();
        return ProntuarioResponse.builder()
                .id(prontuario.getId())
                .exameId(exame.getId())
                .dataAtendimento(exame.getDataHora())
                .tipoAtendimento(exame.getTipo())
                .petId(exame.getPet().getId())
                .petNome(exame.getPet().getNome())
                .petCode(exame.getPet().getPetCode())
                .veterinarioId(exame.getVeterinario().getId())
                .veterinarioNome(exame.getVeterinario().getNome())
                .diagnostico(prontuario.getDiagnostico())
                .prescricao(prontuario.getPrescricao())
                .examesSolicitados(prontuario.getExamesSolicitados())
                .observacoes(prontuario.getObservacoes())
                .pesoAferidoKg(prontuario.getPesoAferidoKg())
                .proximoRetorno(prontuario.getProximoRetorno())
                .registradoEm(prontuario.getRegistradoEm())
                .atualizadoEm(prontuario.getAtualizadoEm())
                .build();
    }
}
