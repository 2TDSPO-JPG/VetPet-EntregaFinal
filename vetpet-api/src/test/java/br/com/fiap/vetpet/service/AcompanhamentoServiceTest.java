package br.com.fiap.vetpet.service;

import br.com.fiap.vetpet.domain.enums.SituacaoSaude;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AcompanhamentoServiceTest {

    @Test
    void petSemAtendimentoNaoPossuiHistorico() {
        assertThat(AcompanhamentoService.avaliar(null)).isEqualTo(SituacaoSaude.SEM_HISTORICO);
    }

    @Test
    void petAtendidoNoUltimoAnoEstaEmDia() {
        assertThat(AcompanhamentoService.avaliar(LocalDateTime.now().minusMonths(11)))
                .isEqualTo(SituacaoSaude.EM_DIA);
    }

    @Test
    void petSemCheckupHaMaisDeUmAnoPrecisaDeAtencao() {
        assertThat(AcompanhamentoService.avaliar(LocalDateTime.now().minusMonths(13)))
                .isEqualTo(SituacaoSaude.ATENCAO);
    }
}
