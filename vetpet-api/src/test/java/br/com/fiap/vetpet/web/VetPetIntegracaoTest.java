package br.com.fiap.vetpet.web;

import br.com.fiap.vetpet.domain.enums.Especie;
import br.com.fiap.vetpet.domain.enums.Perfil;
import br.com.fiap.vetpet.domain.enums.TipoAtendimento;
import br.com.fiap.vetpet.dto.request.ExameRequest;
import br.com.fiap.vetpet.dto.request.PerguntaRequest;
import br.com.fiap.vetpet.dto.request.PetRequest;
import br.com.fiap.vetpet.dto.request.ProntuarioRequest;
import br.com.fiap.vetpet.dto.request.TutorRequest;
import br.com.fiap.vetpet.dto.request.VeterinarioRequest;
import br.com.fiap.vetpet.dto.response.ExameResponse;
import br.com.fiap.vetpet.dto.response.PetResponse;
import br.com.fiap.vetpet.dto.response.ProntuarioResponse;
import br.com.fiap.vetpet.dto.response.TutorResponse;
import br.com.fiap.vetpet.dto.response.VeterinarioResponse;
import br.com.fiap.vetpet.security.UsuarioAutenticado;
import br.com.fiap.vetpet.service.ExameService;
import br.com.fiap.vetpet.service.PerguntaService;
import br.com.fiap.vetpet.service.PetService;
import br.com.fiap.vetpet.service.ProntuarioService;
import br.com.fiap.vetpet.service.TutorService;
import br.com.fiap.vetpet.service.VeterinarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class VetPetIntegracaoTest {

    private static final AtomicInteger SEQUENCIA = new AtomicInteger();
    private static final String ADMIN_EMAIL = "admin@teste.com";
    private static final String ADMIN_SENHA = "senha-teste";

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private VeterinarioService veterinarioService;

    @Autowired
    private PetService petService;

    @Autowired
    private ExameService exameService;

    @Autowired
    private ProntuarioService prontuarioService;

    @Autowired
    private PerguntaService perguntaService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void paginasPublicasCarregamSemLogin() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
    }

    @Test
    void painelExigeLogin() throws Exception {
        mockMvc.perform(get("/dashboard")).andExpect(status().is3xxRedirection());
    }

    @Test
    void apiExigeAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/pets")).andExpect(status().isUnauthorized());
    }

    @Test
    void tutorNaoAcessaRotasDaClinica() throws Exception {
        String email = "tutor" + SEQUENCIA.incrementAndGet() + "@teste.com";
        tutorService.cadastrar(tutor(email));

        mockMvc.perform(get("/api/v1/pets").with(httpBasic(email, "segredo1")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/me/pets").with(httpBasic(email, "segredo1")))
                .andExpect(status().isOk());
    }

    @Test
    void cadastroPublicoDeTutorValidaCampos() throws Exception {
        mockMvc.perform(post("/api/v1/tutores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"invalido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..erros.nome").exists())
                .andExpect(jsonPath("$..erros.email").exists());
    }

    @Test
    void cadastroPublicoDeTutorRetornaCreated() throws Exception {
        int numero = SEQUENCIA.incrementAndGet();
        String corpo = """
                {"nome":"Tutor API","cpf":"%s","email":"api%d@teste.com","senha":"segredo1"}
                """.formatted(cpf(numero), numero);

        mockMvc.perform(post("/api/v1/tutores").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("api" + numero + "@teste.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void fluxoCompletoGeraAcompanhamentoELinhaDoTempo() throws Exception {
        Cenario cenario = criarCenario();

        mockMvc.perform(get("/api/v1/pets/{id}/acompanhamento", cenario.pet().getId())
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacaoSaude").value("EM_DIA"))
                .andExpect(jsonPath("$.prontuarios.length()").value(1))
                .andExpect(jsonPath("$.linhaDoTempo.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));
    }

    @Test
    void apiRetornaProblemDetailParaRecursoInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/pets/999999").with(httpBasic(ADMIN_EMAIL, ADMIN_SENHA)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"));
    }

    @Test
    void todasAsTelasRenderizamParaAEquipe() throws Exception {
        Cenario cenario = criarCenario();
        RequestPostProcessor admin = SecurityMockMvcRequestPostProcessors.user(
                new UsuarioAutenticado(ADMIN_EMAIL, "hash", "Administrador VetPet", Perfil.ADMIN));
        List<String> telas = List.of(
                "/dashboard",
                "/pets", "/pets?busca=Thor", "/pets/novo", "/pets/" + cenario.pet().getId(),
                "/pets/" + cenario.pet().getId() + "/editar",
                "/tutores", "/tutores/novo", "/tutores/" + cenario.tutor().getId(),
                "/tutores/" + cenario.tutor().getId() + "/editar",
                "/veterinarios", "/veterinarios/novo", "/veterinarios/" + cenario.veterinario().getId() + "/editar",
                "/atendimentos", "/atendimentos?status=AGENDADO", "/atendimentos/novo?petId=" + cenario.pet().getId(),
                "/prontuarios", "/prontuarios/" + cenario.prontuario().getId(),
                "/prontuarios/" + cenario.prontuario().getId() + "/editar",
                "/prontuarios/novo?exameId=" + cenario.atendimentoAberto().getId(),
                "/notificacoes");
        for (String tela : telas) {
            mockMvc.perform(get(tela).with(admin))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
        }
    }

    @Test
    void formularioInvalidoMostraErrosNaPropriaTela() throws Exception {
        RequestPostProcessor admin = SecurityMockMvcRequestPostProcessors.user(
                new UsuarioAutenticado(ADMIN_EMAIL, "hash", "Administrador VetPet", Perfil.ADMIN));

        mockMvc.perform(post("/tutores/novo").with(admin).with(csrf()).param("nome", "").param("email", "x"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Informe o nome")));
    }

    @Test
    void formularioValidoRedirecionaComMensagem() throws Exception {
        int numero = SEQUENCIA.incrementAndGet();
        RequestPostProcessor admin = SecurityMockMvcRequestPostProcessors.user(
                new UsuarioAutenticado(ADMIN_EMAIL, "hash", "Administrador VetPet", Perfil.ADMIN));

        mockMvc.perform(post("/tutores/novo").with(admin).with(csrf())
                        .param("nome", "Tutor Tela")
                        .param("cpf", cpf(numero))
                        .param("email", "tela" + numero + "@teste.com")
                        .param("senha", "segredo1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void veterinarioNaoGerenciaEquipe() throws Exception {
        RequestPostProcessor veterinario = SecurityMockMvcRequestPostProcessors.user(
                new UsuarioAutenticado("vet@teste.com", "hash", "Vet Teste", Perfil.VETERINARIO));

        mockMvc.perform(get("/veterinarios").with(veterinario)).andExpect(status().isOk());
        mockMvc.perform(get("/veterinarios/novo").with(veterinario)).andExpect(status().isForbidden());
    }

    private Cenario criarCenario() {
        int numero = SEQUENCIA.incrementAndGet();
        TutorResponse tutor = tutorService.cadastrar(tutor("cenario" + numero + "@teste.com"));

        VeterinarioRequest vetRequest = new VeterinarioRequest();
        vetRequest.setNome("Vet " + numero);
        vetRequest.setCrmv("CRMV-SP " + (10000 + numero));
        vetRequest.setEmail("vet" + numero + "@teste.com");
        vetRequest.setSenha("segredo1");
        VeterinarioResponse veterinario = veterinarioService.cadastrar(vetRequest);

        PetRequest petRequest = new PetRequest();
        petRequest.setNome("Thor");
        petRequest.setEspecie(Especie.CACHORRO);
        petRequest.setDataNascimento(LocalDate.of(2019, 5, 10));
        petRequest.setPesoKg(new BigDecimal("30.00"));
        petRequest.setTutoresIds(List.of(tutor.getId()));
        PetResponse pet = petService.cadastrar(petRequest);

        ExameResponse realizado = exameService.agendar(atendimento(pet.getId(), veterinario.getId(), 1));
        ProntuarioRequest prontuarioRequest = new ProntuarioRequest();
        prontuarioRequest.setExameId(realizado.getId());
        prontuarioRequest.setDiagnostico("Saudável");
        prontuarioRequest.setPesoAferidoKg(new BigDecimal("31.00"));
        ProntuarioResponse prontuario = prontuarioService.registrar(prontuarioRequest);

        ExameResponse aberto = exameService.agendar(atendimento(pet.getId(), veterinario.getId(), 2));

        PerguntaRequest pergunta = new PerguntaRequest();
        pergunta.setTexto("Posso trocar a ração?");
        perguntaService.criar(pet.getId(), pergunta, tutor.getNome());

        return new Cenario(tutor, veterinario, pet, prontuario, aberto);
    }

    private ExameRequest atendimento(Long petId, Long veterinarioId, int diasAFrente) {
        ExameRequest request = new ExameRequest();
        request.setPetId(petId);
        request.setVeterinarioId(veterinarioId);
        request.setTipo(TipoAtendimento.CONSULTA);
        request.setDataHora(LocalDateTime.now().plusDays(diasAFrente).withSecond(0).withNano(0));
        return request;
    }

    private TutorRequest tutor(String email) {
        TutorRequest request = new TutorRequest();
        request.setNome("Tutor Teste");
        request.setCpf(cpf(SEQUENCIA.incrementAndGet()));
        request.setEmail(email);
        request.setSenha("segredo1");
        return request;
    }

    private static String cpf(int numero) {
        return String.format("%011d", 10_000_000_000L + numero);
    }

    private record Cenario(TutorResponse tutor, VeterinarioResponse veterinario, PetResponse pet,
                           ProntuarioResponse prontuario, ExameResponse atendimentoAberto) {
    }
}
