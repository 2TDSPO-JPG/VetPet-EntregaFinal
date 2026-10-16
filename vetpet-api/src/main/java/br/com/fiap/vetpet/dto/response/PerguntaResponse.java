package br.com.fiap.vetpet.dto.response;

import br.com.fiap.vetpet.domain.entity.Pergunta;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PerguntaResponse {

    private Long id;
    private Long petId;
    private String petNome;
    private String texto;
    private String autor;
    private String resposta;
    private String respondidaPor;
    private LocalDateTime criadaEm;
    private LocalDateTime respondidaEm;
    private boolean respondida;

    public static PerguntaResponse de(Pergunta pergunta) {
        return PerguntaResponse.builder()
                .id(pergunta.getId())
                .petId(pergunta.getPet().getId())
                .petNome(pergunta.getPet().getNome())
                .texto(pergunta.getTexto())
                .autor(pergunta.getAutor())
                .resposta(pergunta.getResposta())
                .respondidaPor(pergunta.getRespondidaPor())
                .criadaEm(pergunta.getCriadaEm())
                .respondidaEm(pergunta.getRespondidaEm())
                .respondida(pergunta.isRespondida())
                .build();
    }
}
