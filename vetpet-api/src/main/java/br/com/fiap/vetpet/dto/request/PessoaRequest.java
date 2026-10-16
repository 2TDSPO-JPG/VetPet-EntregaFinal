package br.com.fiap.vetpet.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class PessoaRequest {

    @NotBlank(message = "Informe o nome")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    private String nome;

    @Pattern(regexp = "^$|\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "CPF deve ter o formato 000.000.000-00")
    private String cpf;

    @NotBlank(message = "Informe o e-mail")
    @Email(message = "E-mail inválido")
    @Size(max = 120)
    private String email;

    @Pattern(regexp = "^$|[0-9()+\\-\\s]{8,20}", message = "Telefone inválido")
    private String telefone;

    @Size(max = 500)
    private String fotoUrl;

    @NotBlank(groups = Cadastro.class, message = "Informe uma senha")
    @Size(min = 6, max = 60, groups = Cadastro.class, message = "A senha deve ter entre 6 e 60 caracteres")
    private String senha;

    @Valid
    private EnderecoRequest endereco = new EnderecoRequest();
}
