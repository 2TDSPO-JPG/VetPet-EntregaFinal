package br.com.fiap.vetpet.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

    @Column(name = "cep", length = 9)
    private String cep;

    @Column(name = "logradouro", length = 150)
    private String logradouro;

    @Column(name = "numero", length = 10)
    private String numero;

    @Column(name = "complemento", length = 80)
    private String complemento;

    @Column(name = "bairro", length = 80)
    private String bairro;

    @Column(name = "cidade", length = 80)
    private String cidade;

    @Column(name = "estado", length = 2)
    private String estado;
}
