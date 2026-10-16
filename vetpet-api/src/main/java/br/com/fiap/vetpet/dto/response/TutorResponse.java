package br.com.fiap.vetpet.dto.response;

import br.com.fiap.vetpet.domain.entity.Pet;
import br.com.fiap.vetpet.domain.entity.Tutor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
@Builder
public class TutorResponse {

    private Long id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String fotoUrl;
    private EnderecoResponse endereco;
    private LocalDateTime criadoEm;
    private LocalDateTime ultimoAcesso;
    private List<PetResumoResponse> pets;

    public static TutorResponse de(Tutor tutor) {
        return TutorResponse.builder()
                .id(tutor.getId())
                .nome(tutor.getNome())
                .cpf(tutor.getCpf())
                .email(tutor.getEmail())
                .telefone(tutor.getTelefone())
                .fotoUrl(tutor.getFotoUrl())
                .endereco(EnderecoResponse.de(tutor.getEndereco()))
                .criadoEm(tutor.getCriadoEm())
                .ultimoAcesso(tutor.getUltimoAcesso())
                .pets(tutor.getPets().stream()
                        .sorted(Comparator.comparing(Pet::getNome, String.CASE_INSENSITIVE_ORDER))
                        .map(PetResumoResponse::de)
                        .toList())
                .build();
    }

    public int getQuantidadePets() {
        return pets == null ? 0 : pets.size();
    }
}
