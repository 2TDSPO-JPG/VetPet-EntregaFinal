package br.com.fiap.vetpet.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "VP_TUTOR")
@Getter
@Setter
@NoArgsConstructor
public class Tutor extends Pessoa {

    @ManyToMany(mappedBy = "tutores")
    private Set<Pet> pets = new LinkedHashSet<>();
}
