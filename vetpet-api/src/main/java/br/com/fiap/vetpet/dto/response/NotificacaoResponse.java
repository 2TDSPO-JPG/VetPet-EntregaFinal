package br.com.fiap.vetpet.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NotificacaoResponse {

    private Long id;
    private String tipo;
    private String titulo;
    private String mensagem;
    private String petNome;
    private List<String> destinatarios = new ArrayList<>();
    private String canal;
    private LocalDateTime criadaEm;
}
