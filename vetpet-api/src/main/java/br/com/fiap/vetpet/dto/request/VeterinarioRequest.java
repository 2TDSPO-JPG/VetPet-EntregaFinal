package br.com.fiap.vetpet.dto.request;

import br.com.fiap.vetpet.domain.entity.Veterinario;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VeterinarioRequest extends PessoaRequest {

    @NotBlank(message = "Informe o CRMV")
    @Size(max = 20)
    private String crmv;

    @Size(max = 80)
    private String especialidade;

    @Min(value = 0, message = "Experiência não pode ser negativa")
    @Max(value = 70, message = "Experiência inválida")
    private Integer anosExperiencia;

    @Size(max = 1000, message = "A biografia deve ter no máximo 1000 caracteres")
    private String biografia;

    public static VeterinarioRequest de(Veterinario veterinario) {
        VeterinarioRequest request = new VeterinarioRequest();
        request.setNome(veterinario.getNome());
        request.setCpf(veterinario.getCpf());
        request.setEmail(veterinario.getEmail());
        request.setTelefone(veterinario.getTelefone());
        request.setFotoUrl(veterinario.getFotoUrl());
        request.setEndereco(EnderecoRequest.de(veterinario.getEndereco()));
        request.setCrmv(veterinario.getCrmv());
        request.setEspecialidade(veterinario.getEspecialidade());
        request.setAnosExperiencia(veterinario.getAnosExperiencia());
        request.setBiografia(veterinario.getBiografia());
        return request;
    }
}
