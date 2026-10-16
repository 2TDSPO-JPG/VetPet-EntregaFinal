package br.com.fiap.vetpet.integration;

import br.com.fiap.vetpet.dto.response.NotificacaoResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PaginaNotificacoes {

    private List<NotificacaoResponse> conteudo = new ArrayList<>();
    private int pagina;
    private int tamanho;
    private long totalElementos;
    private int totalPaginas;
}
