package br.com.fiap.vetpet.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "VP_VETERINARIO")
@Getter
@Setter
@NoArgsConstructor
public class Veterinario extends Pessoa {

    @Column(name = "crmv", nullable = false, unique = true, length = 20)
    private String crmv;

    @Column(name = "especialidade", length = 80)
    private String especialidade;

    @Column(name = "anos_experiencia")
    private Integer anosExperiencia;

    @Column(name = "biografia", length = 1000)
    private String biografia;
}
