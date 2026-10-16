package br.com.fiap.vetpet.notificacao.service;

import br.com.fiap.vetpet.notificacao.config.EmailProperties;
import br.com.fiap.vetpet.notificacao.domain.Canal;
import br.com.fiap.vetpet.notificacao.messaging.EventoVetPet;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);
    private static final String NOME_REMETENTE = "VetPet";

    private final ObjectProvider<JavaMailSender> mailSender;
    private final EmailProperties properties;

    public EmailService(ObjectProvider<JavaMailSender> mailSender, EmailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public Canal enviar(EventoVetPet evento, List<String> destinatarios) {
        List<String> reais = destinatarios.stream().filter(email -> !properties.deveIgnorar(email)).toList();
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null || !properties.configurado() || reais.isEmpty()) {
            destinatarios.forEach(email -> LOG.info("[E-MAIL SIMULADO] Para: {} | {} | {}", email,
                    evento.titulo(), evento.descricao()));
            return Canal.EMAIL_SIMULADO;
        }
        boolean todosEnviados = true;
        for (String email : reais) {
            todosEnviados &= enviarPara(sender, email, evento);
        }
        return todosEnviados ? Canal.EMAIL : Canal.EMAIL_FALHOU;
    }

    private boolean enviarPara(JavaMailSender sender, String email, EventoVetPet evento) {
        try {
            MimeMessage mensagem = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, false, "UTF-8");
            helper.setFrom(properties.remetente(), NOME_REMETENTE);
            helper.setTo(email);
            helper.setSubject(EmailTemplate.assunto(evento));
            helper.setText(EmailTemplate.html(evento), true);
            sender.send(mensagem);
            LOG.info("E-mail '{}' enviado para {}", evento.titulo(), email);
            return true;
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            LOG.warn("Falha ao enviar e-mail para {}: {}", email, e.getMessage());
            return false;
        }
    }
}
