package br.com.fiap.vetpet.notificacao.web;

import br.com.fiap.vetpet.notificacao.domain.Notificacao;

import java.time.LocalDateTime;
import java.util.List;

public record NotificacaoResponse(
        Long id,
        String tipo,
        String titulo,
        String mensagem,
        String petNome,
        List<String> destinatarios,
        String canal,
        LocalDateTime criadaEm) {

    public static NotificacaoResponse de(Notificacao notificacao) {
        return new NotificacaoResponse(notificacao.getId(), notificacao.getTipo(), notificacao.getTitulo(),
                notificacao.getMensagem(), notificacao.getPetNome(), notificacao.getDestinatarios(),
                notificacao.getCanal().name(), notificacao.getCriadaEm());
    }
}
