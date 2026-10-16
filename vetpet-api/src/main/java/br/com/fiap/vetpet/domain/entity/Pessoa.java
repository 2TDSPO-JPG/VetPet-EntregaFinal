package br.com.fiap.vetpet.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "cpf", length = 14)
    private String cpf;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Embedded
    private Endereco endereco;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "credencial_id", unique = true)
    private Credencial credencial;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;

    @PrePersist
    protected void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    public void definirSenha(String senhaHash) {
        if (credencial == null) {
            credencial = new Credencial(senhaHash);
        } else {
            credencial.setSenhaHash(senhaHash);
        }
    }

    public void registrarAcesso() {
        this.ultimoAcesso = LocalDateTime.now();
    }
}
