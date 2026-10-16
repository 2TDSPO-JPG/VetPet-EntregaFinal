package br.com.fiap.vetpet.notificacao.service;

import br.com.fiap.vetpet.notificacao.messaging.EventoVetPet;
import org.springframework.web.util.HtmlUtils;

import java.time.format.DateTimeFormatter;

final class EmailTemplate {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private EmailTemplate() {
    }

    static String assunto(EventoVetPet evento) {
        String pet = evento.petNome() == null ? "" : " - " + evento.petNome();
        return "VetPet | " + evento.titulo() + pet;
    }

    static String html(EventoVetPet evento) {
        String pet = evento.petNome() == null ? "" : """
                <p style="margin:0 0 6px;font-size:14px;color:#6d6378;">Pet</p>
                <p style="margin:0 0 20px;font-size:18px;font-weight:700;color:#24123a;">%s</p>
                """.formatted(escapar(evento.petNome()));
        String quando = evento.ocorridoEm() == null ? "" : evento.ocorridoEm().format(FORMATO);
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <body style="margin:0;padding:0;background:#f6f4fa;font-family:Arial,Helvetica,sans-serif;color:#2b2236;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f6f4fa;padding:32px 12px;">
                  <tr><td align="center">
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:560px;background:#ffffff;border-radius:14px;overflow:hidden;border:1px solid #e3dceb;">
                      <tr><td style="background:#24123a;padding:22px 28px;">
                        <span style="font-size:22px;font-weight:700;color:#ffffff;">Vet</span><span style="font-size:22px;font-weight:700;color:#e9b8ee;">Pet</span>
                      </td></tr>
                      <tr><td style="padding:28px;">
                        <h1 style="margin:0 0 18px;font-size:22px;color:#24123a;">%s</h1>
                        %s
                        <p style="margin:0 0 24px;font-size:16px;line-height:1.55;color:#4a3a5e;">%s</p>
                        <p style="margin:0;font-size:13px;color:#6d6378;">%s</p>
                      </td></tr>
                      <tr><td style="padding:18px 28px;background:#faf8fc;border-top:1px solid #e3dceb;font-size:12px;color:#6d6378;">
                        Você recebeu este aviso porque é tutor(a) de um pet acompanhado pela clínica no VetPet.
                      </td></tr>
                    </table>
                  </td></tr>
                </table>
                </body>
                </html>
                """.formatted(escapar(evento.titulo()), pet, escapar(evento.descricao()), escapar(quando));
    }

    private static String escapar(String texto) {
        return texto == null ? "" : HtmlUtils.htmlEscape(texto, "UTF-8");
    }
}
