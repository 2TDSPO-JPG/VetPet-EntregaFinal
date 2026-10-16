package br.com.fiap.vetpet.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "VP_PRONTUARIO")
@Getter
@Setter
@NoArgsConstructor
public class Prontuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exame_id", nullable = false, unique = true)
    private Exame exame;

    @Column(name = "diagnostico", nullable = false, length = 2000)
    private String diagnostico;

    @Column(name = "prescricao", length = 2000)
    private String prescricao;

    @Column(name = "exames_solicitados", length = 1000)
    private String examesSolicitados;

    @Column(name = "observacoes", length = 2000)
    private String observacoes;

    @Column(name = "peso_aferido_kg", precision = 6, scale = 2)
    private BigDecimal pesoAferidoKg;

    @Column(name = "proximo_retorno")
    private LocalDate proximoRetorno;

    @Column(name = "registrado_em", nullable = false, updatable = false)
    private LocalDateTime registradoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void aoCriar() {
        if (this.registradoEm == null) {
            this.registradoEm = LocalDateTime.now();
        }
        this.atualizadoEm = this.registradoEm;
    }

    @PreUpdate
    protected void aoAtualizar() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
