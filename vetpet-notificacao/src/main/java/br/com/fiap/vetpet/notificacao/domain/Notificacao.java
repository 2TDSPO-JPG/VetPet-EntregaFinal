package br.com.fiap.vetpet.notificacao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "VP_NOTIFICACAO")
public class Notificacao {

    private static final String SEPARADOR = ";";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false, unique = true, length = 36)
    private String eventoId;

    @Column(name = "tipo", nullable = false, length = 40)
    private String tipo;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "mensagem", length = 1000)
    private String mensagem;

    @Column(name = "pet_nome", length = 80)
    private String petNome;

    @Column(name = "destinatarios", length = 1000)
    private String destinatarios;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false, length = 20)
    private Canal canal;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    protected Notificacao() {
    }

    public Notificacao(String eventoId, String tipo, String titulo, String mensagem, String petNome,
                       List<String> destinatarios, Canal canal) {
        this.eventoId = eventoId;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.petNome = petNome;
        this.destinatarios = String.join(SEPARADOR, destinatarios);
        this.canal = canal;
        this.criadaEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getEventoId() {
        return eventoId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getPetNome() {
        return petNome;
    }

    public List<String> getDestinatarios() {
        if (destinatarios == null || destinatarios.isBlank()) {
            return List.of();
        }
        return Arrays.asList(destinatarios.split(SEPARADOR));
    }

    public Canal getCanal() {
        return canal;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }
}
