package br.com.fiap.vetpet.notificacao;

import br.com.fiap.vetpet.notificacao.config.ApiKeyFilter;
import br.com.fiap.vetpet.notificacao.messaging.EventoVetPet;
import br.com.fiap.vetpet.notificacao.service.NotificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class NotificacaoApplicationTest {

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private NotificacaoService notificacaoService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto)
                .addFilters(contexto.getBean(ApiKeyFilter.class))
                .build();
    }

    @Test
    void apiExigeChave() throws Exception {
        mockMvc.perform(get("/api/notificacoes")).andExpect(status().isUnauthorized());
    }

    @Test
    void listaNotificacoesFiltrandoPorEmail() throws Exception {
        notificacaoService.processar(new EventoVetPet("evt-integracao", "EXAME_AGENDADO", 1L, "Luna",
                List.of("juliana@email.com"), "Atendimento agendado", "Exame da Luna.", LocalDateTime.now()));

        mockMvc.perform(get("/api/notificacoes").param("email", "juliana@email.com")
                        .header("X-API-KEY", "chave-teste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].petNome").value("Luna"));
    }
}
