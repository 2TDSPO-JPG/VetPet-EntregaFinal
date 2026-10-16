package br.com.fiap.vetpet.config;

import br.com.fiap.vetpet.domain.entity.Endereco;
import br.com.fiap.vetpet.domain.entity.Exame;
import br.com.fiap.vetpet.domain.entity.Pet;
import br.com.fiap.vetpet.domain.entity.Prontuario;
import br.com.fiap.vetpet.domain.entity.Tutor;
import br.com.fiap.vetpet.domain.entity.Veterinario;
import br.com.fiap.vetpet.domain.enums.Especie;
import br.com.fiap.vetpet.domain.enums.Sexo;
import br.com.fiap.vetpet.domain.enums.StatusExame;
import br.com.fiap.vetpet.domain.enums.TipoAtendimento;
import br.com.fiap.vetpet.domain.enums.TipoEvento;
import br.com.fiap.vetpet.dto.request.ExameRequest;
import br.com.fiap.vetpet.dto.request.PerguntaRequest;
import br.com.fiap.vetpet.dto.request.PetRequest;
import br.com.fiap.vetpet.dto.request.RespostaRequest;
import br.com.fiap.vetpet.dto.request.TutorRequest;
import br.com.fiap.vetpet.dto.request.VeterinarioRequest;
import br.com.fiap.vetpet.dto.response.PerguntaResponse;
import br.com.fiap.vetpet.dto.response.PetResponse;
import br.com.fiap.vetpet.dto.response.TutorResponse;
import br.com.fiap.vetpet.dto.response.VeterinarioResponse;
import br.com.fiap.vetpet.messaging.EventoVetPet;
import br.com.fiap.vetpet.repository.ExameRepository;
import br.com.fiap.vetpet.repository.PetRepository;
import br.com.fiap.vetpet.repository.TutorRepository;
import br.com.fiap.vetpet.repository.VeterinarioRepository;
import br.com.fiap.vetpet.service.ExameService;
import br.com.fiap.vetpet.service.HistoricoService;
import br.com.fiap.vetpet.service.PerguntaService;
import br.com.fiap.vetpet.service.PetService;
import br.com.fiap.vetpet.service.TutorService;
import br.com.fiap.vetpet.service.VeterinarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "vetpet.seed.habilitado", havingValue = "true")
public class DadosDemonstracao implements ApplicationRunner {

    private static final String SENHA_VETERINARIO = "vet12345";
    private static final String SENHA_TUTOR = "tutor123";
    private static final String EMAIL_PEDRO = "pedroferreira.gomes03@gmail.com";

    private final TutorService tutorService;
    private final VeterinarioService veterinarioService;
    private final PetService petService;
    private final ExameService exameService;
    private final PerguntaService perguntaService;
    private final HistoricoService historicoService;
    private final TutorRepository tutorRepository;
    private final PetRepository petRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ExameRepository exameRepository;
    private final TransactionTemplate transactionTemplate;
    private final PasswordEncoder passwordEncoder;

    public DadosDemonstracao(TutorService tutorService, VeterinarioService veterinarioService, PetService petService,
                             ExameService exameService, PerguntaService perguntaService,
                             HistoricoService historicoService, TutorRepository tutorRepository,
                             PetRepository petRepository, VeterinarioRepository veterinarioRepository,
                             ExameRepository exameRepository, PlatformTransactionManager transactionManager,
                             PasswordEncoder passwordEncoder) {
        this.tutorService = tutorService;
        this.veterinarioService = veterinarioService;
        this.petService = petService;
        this.exameService = exameService;
        this.perguntaService = perguntaService;
        this.historicoService = historicoService;
        this.tutorRepository = tutorRepository;
        this.petRepository = petRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.exameRepository = exameRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (tutorRepository.count() == 0) {
            criarDemonstracao();
        }
        garantirTutorPedro();
    }

    private void criarDemonstracao() {
        VeterinarioResponse ana = veterinario("Ana Ribeiro", "CRMV-SP 12345", "ana@vetpet.com", "Clínica geral", 9);
        VeterinarioResponse carlos = veterinario("Carlos Mendes", "CRMV-SP 23456", "carlos@vetpet.com", "Cardiologia", 15);

        TutorResponse mariana = tutor("Mariana Costa", "123.456.789-09", "mariana@example.com", "(11) 98888-1234");
        TutorResponse rafael = tutor("Rafael Souza", "987.654.321-00", "rafael@example.com", "(11) 97777-5678");
        TutorResponse juliana = tutor("Juliana Lima", "111.444.777-35", "juliana@example.com", "(11) 96666-9012");

        PetResponse thor = pet("Thor", Especie.CACHORRO, "Golden Retriever", Sexo.MACHO, LocalDate.of(2019, 5, 10),
                "32.50", null, List.of(mariana.getId(), rafael.getId()));
        PetResponse luna = pet("Luna", Especie.GATO, "SRD", Sexo.FEMEA, LocalDate.of(2021, 8, 22),
                "4.20", "Dipirona", List.of(mariana.getId()));
        PetResponse bidu = pet("Bidu", Especie.CACHORRO, "Shih Tzu", Sexo.MACHO, LocalDate.of(2015, 2, 1),
                "7.80", null, List.of(juliana.getId()));
        pet("Pipoca", Especie.AVE, "Calopsita", Sexo.FEMEA, LocalDate.of(2023, 11, 3),
                "0.09", null, List.of(rafael.getId()));

        atendimentoPassado(thor.getId(), ana.getId(), TipoAtendimento.CONSULTA, LocalDateTime.now().minusMonths(2),
                "Check-up anual", "Saudável. Leve tártaro dentário.", "Escovação diária e petisco dental.",
                "32.50", LocalDate.now().plusMonths(10));
        atendimentoPassado(luna.getId(), ana.getId(), TipoAtendimento.VACINA, LocalDateTime.now().minusMonths(1),
                "Reforço da vacina quádrupla felina", "Vacinação realizada sem intercorrências.", null,
                "4.20", LocalDate.now().plusMonths(11));
        atendimentoPassado(bidu.getId(), carlos.getId(), TipoAtendimento.CONSULTA, LocalDateTime.now().minusMonths(14),
                "Avaliação cardiológica", "Sopro cardíaco grau II, acompanhamento recomendado.",
                "Pimobendan 1,25 mg a cada 12h.", "7.80", LocalDate.now().minusMonths(8));

        agendar(thor.getId(), ana.getId(), TipoAtendimento.RETORNO, LocalDate.now().plusDays(2).atTime(10, 0),
                "Reavaliação dentária");
        agendar(luna.getId(), carlos.getId(), TipoAtendimento.EXAME_LABORATORIAL,
                LocalDate.now().plusDays(1).atTime(9, 30), "Hemograma de rotina");
        agendar(bidu.getId(), carlos.getId(), TipoAtendimento.CONSULTA, LocalDate.now().plusDays(5).atTime(15, 0),
                "Retorno cardiológico atrasado");

        PerguntaResponse duvidaLuna = perguntaService.criar(luna.getId(),
                pergunta("A Luna pode tomar banho depois da vacina?"), mariana.getNome());
        RespostaRequest resposta = new RespostaRequest();
        resposta.setResposta("Recomendamos aguardar 7 dias após a vacina para o banho.");
        perguntaService.responder(duvidaLuna.getId(), resposta, ana.getNome());
        perguntaService.criar(bidu.getId(), pergunta("O Bidu está tossindo à noite. Devo antecipar a consulta?"),
                juliana.getNome());

        log.info("Dados de demonstração criados. Veterinários: ana@vetpet.com / carlos@vetpet.com (senha {}).",
                SENHA_VETERINARIO);
    }

    private void garantirTutorPedro() {
        transactionTemplate.executeWithoutResult(status -> {
            if (tutorRepository.existsByEmailIgnoreCase(EMAIL_PEDRO)) {
                return;
            }
            Tutor pedro = new Tutor();
            pedro.setNome("Pedro Ferreira");
            pedro.setEmail(EMAIL_PEDRO);
            pedro.setCpf("529.982.247-25");
            pedro.setEndereco(new Endereco(null, null, null, null, null, "São Paulo", "SP"));
            pedro.definirSenha(passwordEncoder.encode(SENHA_TUTOR));
            tutorRepository.save(pedro);
            petRepository.findAll().stream()
                    .filter(pet -> "Thor".equals(pet.getNome()))
                    .findFirst()
                    .ifPresent(pet -> pet.adicionarTutor(pedro));
            log.info("Tutor Pedro Ferreira ({}) disponível para receber notificações.", EMAIL_PEDRO);
        });
    }

    private VeterinarioResponse veterinario(String nome, String crmv, String email, String especialidade, int anos) {
        VeterinarioRequest request = new VeterinarioRequest();
        request.setNome(nome);
        request.setCrmv(crmv);
        request.setEmail(email);
        request.setEspecialidade(especialidade);
        request.setAnosExperiencia(anos);
        request.setTelefone("(11) 3333-0000");
        request.setSenha(SENHA_VETERINARIO);
        return veterinarioService.cadastrar(request);
    }

    private TutorResponse tutor(String nome, String cpf, String email, String telefone) {
        TutorRequest request = new TutorRequest();
        request.setNome(nome);
        request.setCpf(cpf);
        request.setEmail(email);
        request.setTelefone(telefone);
        request.setSenha(SENHA_TUTOR);
        request.getEndereco().setCidade("São Paulo");
        request.getEndereco().setEstado("SP");
        return tutorService.cadastrar(request);
    }

    private PetResponse pet(String nome, Especie especie, String raca, Sexo sexo, LocalDate nascimento,
                            String peso, String alergias, List<Long> tutores) {
        PetRequest request = new PetRequest();
        request.setNome(nome);
        request.setEspecie(especie);
        request.setRaca(raca);
        request.setSexo(sexo);
        request.setDataNascimento(nascimento);
        request.setPesoKg(new BigDecimal(peso));
        request.setAlergias(alergias);
        request.setTutoresIds(tutores);
        return petService.cadastrar(request);
    }

    private void agendar(Long petId, Long veterinarioId, TipoAtendimento tipo, LocalDateTime dataHora, String motivo) {
        ExameRequest request = new ExameRequest();
        request.setPetId(petId);
        request.setVeterinarioId(veterinarioId);
        request.setTipo(tipo);
        request.setDataHora(dataHora);
        request.setMotivo(motivo);
        exameService.agendar(request);
    }

    private PerguntaRequest pergunta(String texto) {
        PerguntaRequest request = new PerguntaRequest();
        request.setTexto(texto);
        return request;
    }

    private void atendimentoPassado(Long petId, Long veterinarioId, TipoAtendimento tipo, LocalDateTime data,
                                    String motivo, String diagnostico, String prescricao, String peso,
                                    LocalDate retorno) {
        EventoVetPet evento = transactionTemplate.execute(status -> {
            Pet pet = petRepository.findById(petId).orElseThrow();
            Veterinario veterinario = veterinarioRepository.findById(veterinarioId).orElseThrow();
            Exame exame = new Exame();
            exame.setPet(pet);
            exame.setVeterinario(veterinario);
            exame.setTipo(tipo);
            exame.setDataHora(data);
            exame.setMotivo(motivo);
            exame.setStatus(StatusExame.REALIZADO);
            exame.setLembreteEnviado(true);
            Prontuario prontuario = new Prontuario();
            prontuario.setExame(exame);
            prontuario.setDiagnostico(diagnostico);
            prontuario.setPrescricao(prescricao);
            prontuario.setPesoAferidoKg(new BigDecimal(peso));
            prontuario.setProximoRetorno(retorno);
            prontuario.setRegistradoEm(data.plusHours(1));
            exame.setProntuario(prontuario);
            exameRepository.save(exame);
            return new EventoVetPet(UUID.randomUUID().toString(), TipoEvento.PRONTUARIO_REGISTRADO, pet.getId(),
                    pet.getNome(), pet.emailsTutores(), TipoEvento.PRONTUARIO_REGISTRADO.getDescricao(),
                    tipo.getDescricao() + " realizado(a) por " + veterinario.getNome() + ". Diagnóstico: " + diagnostico,
                    data.plusHours(1));
        });
        historicoService.registrar(evento);
    }
}
