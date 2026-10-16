package br.com.fiap.vetpet.dto.request;

import br.com.fiap.vetpet.domain.entity.Tutor;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TutorRequest extends PessoaRequest {

    @NotBlank(message = "Informe o CPF")
    @Override
    public String getCpf() {
        return super.getCpf();
    }

    public static TutorRequest de(Tutor tutor) {
        TutorRequest request = new TutorRequest();
        request.setNome(tutor.getNome());
        request.setCpf(tutor.getCpf());
        request.setEmail(tutor.getEmail());
        request.setTelefone(tutor.getTelefone());
        request.setFotoUrl(tutor.getFotoUrl());
        request.setEndereco(EnderecoRequest.de(tutor.getEndereco()));
        return request;
    }
}
