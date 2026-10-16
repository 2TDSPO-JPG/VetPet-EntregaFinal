package br.com.fiap.vetpet.messaging;

import br.com.fiap.vetpet.service.ExameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LembreteScheduler {

    private final ExameService exameService;

    @Scheduled(cron = "${vetpet.lembretes.cron}")
    public void enviarLembretes() {
        int enviados = exameService.enviarLembretes();
        if (enviados > 0) {
            log.info("{} lembrete(s) de atendimento publicado(s)", enviados);
        }
    }
}
