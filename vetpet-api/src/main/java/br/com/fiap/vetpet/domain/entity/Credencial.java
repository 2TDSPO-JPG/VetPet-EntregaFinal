package br.com.fiap.vetpet.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "VP_CREDENCIAL")
@Getter
@Setter
@NoArgsConstructor
public class Credencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    public Credencial(String senhaHash) {
        this.senhaHash = senhaHash;
    }
}
