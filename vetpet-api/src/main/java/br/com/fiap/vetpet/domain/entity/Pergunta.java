package br.com.fiap.vetpet.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "VP_PERGUNTA")
@Getter
@Setter
@NoArgsConstructor
public class Pergunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(name = "texto", nullable = false, length = 1000)
    private String texto;

    @Column(name = "autor", length = 120)
    private String autor;

    @Column(name = "resposta", length = 2000)
    private String resposta;

    @Column(name = "respondida_por", length = 120)
    private String respondidaPor;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Column(name = "respondida_em")
    private LocalDateTime respondidaEm;

    @PrePersist
    protected void aoCriar() {
        this.criadaEm = LocalDateTime.now();
    }

    public boolean isRespondida() {
        return resposta != null && !resposta.isBlank();
    }
}
