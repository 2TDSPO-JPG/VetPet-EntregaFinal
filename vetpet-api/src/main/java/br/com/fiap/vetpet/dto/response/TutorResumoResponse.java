package br.com.fiap.vetpet.dto.response;

import br.com.fiap.vetpet.domain.entity.Tutor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TutorResumoResponse {

    private Long id;
    private String nome;
    private String email;
    private String telefone;

    public static TutorResumoResponse de(Tutor tutor) {
        return new TutorResumoResponse(tutor.getId(), tutor.getNome(), tutor.getEmail(), tutor.getTelefone());
    }
}
