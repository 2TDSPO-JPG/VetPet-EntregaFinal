package br.com.fiap.vetpet.dto.response;

import br.com.fiap.vetpet.domain.entity.Veterinario;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VeterinarioResponse {

    private Long id;
    private String nome;
    private String crmv;
    private String cpf;
    private String email;
    private String telefone;
    private String especialidade;
    private Integer anosExperiencia;
    private String biografia;
    private String fotoUrl;
    private EnderecoResponse endereco;

    public static VeterinarioResponse de(Veterinario veterinario) {
        return VeterinarioResponse.builder()
                .id(veterinario.getId())
                .nome(veterinario.getNome())
                .crmv(veterinario.getCrmv())
                .cpf(veterinario.getCpf())
                .email(veterinario.getEmail())
                .telefone(veterinario.getTelefone())
                .especialidade(veterinario.getEspecialidade())
                .anosExperiencia(veterinario.getAnosExperiencia())
                .biografia(veterinario.getBiografia())
                .fotoUrl(veterinario.getFotoUrl())
                .endereco(EnderecoResponse.de(veterinario.getEndereco()))
                .build();
    }
}
