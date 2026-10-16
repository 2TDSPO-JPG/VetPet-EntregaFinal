package br.com.fiap.vetpet.dto.response;

import br.com.fiap.vetpet.domain.entity.Endereco;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Builder
public class EnderecoResponse {

    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;

    public static EnderecoResponse de(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        return EnderecoResponse.builder()
                .cep(endereco.getCep())
                .logradouro(endereco.getLogradouro())
                .numero(endereco.getNumero())
                .complemento(endereco.getComplemento())
                .bairro(endereco.getBairro())
                .cidade(endereco.getCidade())
                .estado(endereco.getEstado())
                .build();
    }

    public String getResumo() {
        String rua = Stream.of(logradouro, numero).filter(Objects::nonNull).filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
        String local = Stream.of(bairro, cidade, estado).filter(Objects::nonNull).filter(s -> !s.isBlank())
                .collect(Collectors.joining(" - "));
        return Stream.of(rua, local).filter(s -> !s.isBlank()).collect(Collectors.joining(", "));
    }
}
