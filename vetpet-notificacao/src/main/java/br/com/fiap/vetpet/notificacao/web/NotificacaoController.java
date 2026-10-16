package br.com.fiap.vetpet.notificacao.web;

import br.com.fiap.vetpet.notificacao.service.NotificacaoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private static final int TAMANHO_MAXIMO = 100;

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public PaginaResponse<NotificacaoResponse> listar(@RequestParam(defaultValue = "") String email,
                                                      @RequestParam(defaultValue = "0") int pagina,
                                                      @RequestParam(defaultValue = "20") int tamanho) {
        PageRequest pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO),
                Sort.by(Sort.Direction.DESC, "criadaEm"));
        return PaginaResponse.de(notificacaoService.listar(email, pageable).map(NotificacaoResponse::de));
    }
}
