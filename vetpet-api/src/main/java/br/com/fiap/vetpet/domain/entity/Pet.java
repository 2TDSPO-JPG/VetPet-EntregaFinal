package br.com.fiap.vetpet.domain.entity;

import br.com.fiap.vetpet.domain.enums.Especie;
import br.com.fiap.vetpet.domain.enums.Sexo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "VP_PET")
@Getter
@Setter
@NoArgsConstructor
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pet_code", nullable = false, unique = true, length = 12)
    private String petCode;

    @Column(name = "nome", nullable = false, length = 80)
    private String nome;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "especie", nullable = false, length = 20)
    private Especie especie;

    @Column(name = "raca", length = 80)
    private String raca;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", length = 10)
    private Sexo sexo;

    @Column(name = "peso_kg", precision = 6, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "alergias", length = 500)
    private String alergias;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @ManyToMany
    @JoinTable(
            name = "VP_TUTOR_PET",
            joinColumns = @JoinColumn(name = "pet_id"),
            inverseJoinColumns = @JoinColumn(name = "tutor_id")
    )
    private Set<Tutor> tutores = new LinkedHashSet<>();

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Exame> exames = new ArrayList<>();

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pergunta> perguntas = new ArrayList<>();

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventoPet> eventos = new ArrayList<>();

    @PrePersist
    protected void aoCriar() {
        this.criadoEm = LocalDateTime.now();
        if (this.petCode == null || this.petCode.isBlank()) {
            this.petCode = "PET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    public int calcularIdade() {
        if (dataNascimento == null) {
            return 0;
        }
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    public void adicionarTutor(Tutor tutor) {
        tutores.add(tutor);
        tutor.getPets().add(this);
    }

    public void removerTutor(Tutor tutor) {
        tutores.remove(tutor);
        tutor.getPets().remove(this);
    }

    public List<String> emailsTutores() {
        return tutores.stream().map(Tutor::getEmail).toList();
    }
}
