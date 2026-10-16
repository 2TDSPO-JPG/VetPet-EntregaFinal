package br.com.fiap.vetpet.dto.request;

import br.com.fiap.vetpet.domain.entity.Endereco;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EnderecoRequest {

    @Pattern(regexp = "^$|\\d{5}-?\\d{3}", message = "CEP deve ter o formato 00000-000")
    private String cep;

    @Size(max = 150)
    private String logradouro;

    @Size(max = 10)
    private String numero;

    @Size(max = 80)
    private String complemento;

    @Size(max = 80)
    private String bairro;

    @Size(max = 80)
    private String cidade;

    @Pattern(regexp = "^$|[A-Za-z]{2}", message = "Use a sigla do estado com 2 letras")
    private String estado;

    public static EnderecoRequest de(Endereco endereco) {
        EnderecoRequest request = new EnderecoRequest();
        if (endereco != null) {
            request.setCep(endereco.getCep());
            request.setLogradouro(endereco.getLogradouro());
            request.setNumero(endereco.getNumero());
            request.setComplemento(endereco.getComplemento());
            request.setBairro(endereco.getBairro());
            request.setCidade(endereco.getCidade());
            request.setEstado(endereco.getEstado());
        }
        return request;
    }

    public Endereco paraEntidade() {
        if (estaVazio()) {
            return null;
        }
        String uf = estado == null || estado.isBlank() ? null : estado.toUpperCase();
        return new Endereco(cep, logradouro, numero, complemento, bairro, cidade, uf);
    }

    private boolean estaVazio() {
        return isBlank(cep) && isBlank(logradouro) && isBlank(numero) && isBlank(complemento)
                && isBlank(bairro) && isBlank(cidade) && isBlank(estado);
    }

    private static boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }
}
