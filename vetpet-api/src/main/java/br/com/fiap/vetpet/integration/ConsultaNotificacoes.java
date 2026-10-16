package br.com.fiap.vetpet.integration;

import br.com.fiap.vetpet.dto.response.NotificacaoResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ConsultaNotificacoes {

    private final boolean disponivel;
    private final List<NotificacaoResponse> notificacoes;
    private final long total;

    public static ConsultaNotificacoes de(PaginaNotificacoes pagina) {
        return new ConsultaNotificacoes(true, pagina.getConteudo(), pagina.getTotalElementos());
    }

    public static ConsultaNotificacoes indisponivel() {
        return new ConsultaNotificacoes(false, List.of(), 0);
    }
}
