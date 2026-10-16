package br.com.fiap.vetpet.domain.entity;

import br.com.fiap.vetpet.domain.enums.StatusExame;
import br.com.fiap.vetpet.domain.enums.TipoAtendimento;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "VP_EXAME")
@Getter
@Setter
@NoArgsConstructor
public class Exame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoAtendimento tipo;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusExame status;

    @Column(name = "lembrete_enviado", nullable = false)
    private boolean lembreteEnviado;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Veterinario veterinario;

    @OneToOne(mappedBy = "exame", cascade = CascadeType.ALL, orphanRemoval = true)
    private Prontuario prontuario;

    @PrePersist
    protected void aoCriar() {
        this.criadoEm = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusExame.AGENDADO;
        }
    }
}
