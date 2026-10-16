package br.com.fiap.vetpet.notificacao.service;

import br.com.fiap.vetpet.notificacao.domain.Canal;
import br.com.fiap.vetpet.notificacao.domain.Notificacao;
import br.com.fiap.vetpet.notificacao.messaging.EventoVetPet;
import br.com.fiap.vetpet.notificacao.repository.NotificacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NotificacaoService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacaoService.class);
    private static final String EVENTO_PARA_EQUIPE = "PERGUNTA_CRIADA";

    private final NotificacaoRepository notificacaoRepository;
    private final EmailService emailService;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, EmailService emailService) {
        this.notificacaoRepository = notificacaoRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Optional<Notificacao> processar(EventoVetPet evento) {
        if (notificacaoRepository.existsByEventoId(evento.id())) {
            LOG.info("Evento {} já processado, ignorando duplicidade", evento.id());
            return Optional.empty();
        }
        boolean paraEquipe = EVENTO_PARA_EQUIPE.equals(evento.tipo());
        List<String> destinatarios = paraEquipe || evento.destinatarios() == null ? List.of() : evento.destinatarios();
        Canal canal;
        if (paraEquipe) {
            canal = Canal.PAINEL_CLINICA;
            LOG.info("[PAINEL DA CLÍNICA] {} | {}", evento.titulo(), evento.descricao());
        } else {
            canal = emailService.enviar(evento, destinatarios);
        }
        return Optional.of(notificacaoRepository.save(new Notificacao(evento.id(), evento.tipo(), evento.titulo(),
                evento.descricao(), evento.petNome(), destinatarios, canal)));
    }

    @Transactional(readOnly = true)
    public Page<Notificacao> listar(String email, Pageable pageable) {
        if (email == null || email.isBlank()) {
            return notificacaoRepository.findAll(pageable);
        }
        return notificacaoRepository.findByDestinatariosContainingIgnoreCase(email.trim(), pageable);
    }
}
