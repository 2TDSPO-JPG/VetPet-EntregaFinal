# VetPet

**Acompanhamento contínuo da saúde de pets para clínicas veterinárias.**

O VetPet transforma cada cadastro, agendamento, prontuário e dúvida de tutor em um evento na **linha da vida do pet**. A clínica enxerga em um só lugar quem está com o check-up atrasado, o que foi prescrito e o que vem a seguir. Os tutores são avisados automaticamente por um microsserviço de notificações conectado via **RabbitMQ**.

| | |
|---|---|
| **Aplicação online** | https://vetpet-api-rm565824.azurewebsites.net |
| **Documentação da API (Swagger)** | https://vetpet-api-rm565824.azurewebsites.net/swagger-ui.html |
| **Projeto no Azure DevOps** | https://dev.azure.com/VetPet/Sprint%204%20%E2%80%93%20Azure%20DevOps |
| **Vídeo DevOps (CI/CD na Azure)** | https://youtu.be/78HX-WVhodQ |
| **Vídeo Java (apresentação da solução)** | `adicione aqui o link do YouTube` |
| **Disciplinas integradas** | Java Advanced + Cloud / DevOps |

---

## Sumário

1. [Equipe](#equipe)
2. [O desafio](#o-desafio)
3. [A solução](#a-solução)
4. [Arquitetura](#arquitetura)
5. [Mensageria com RabbitMQ](#mensageria-com-rabbitmq)
6. [Tecnologias e justificativas](#tecnologias-e-justificativas)
7. [Modelo de dados](#modelo-de-dados)
8. [Telas (Thymeleaf)](#telas-thymeleaf)
9. [API REST](#api-rest)
10. [Segurança](#segurança)
11. [Como executar](#como-executar)
12. [Deploy (Cloud / DevOps)](#deploy-cloud--devops)
13. [Testes e qualidade](#testes-e-qualidade)
14. [Integração multidisciplinar](#integração-multidisciplinar)
15. [Boas práticas aplicadas](#boas-práticas-aplicadas)
16. [Evolução desde as sprints anteriores](#evolução-desde-as-sprints-anteriores)
17. [Organização do repositório](#organização-do-repositório)

---

## Equipe

| Integrante | RM | Responsabilidades principais |
|---|---|---|
| Pedro Ferreira | RM565824 | Arquitetura, mensageria (RabbitMQ), microsserviço de notificação |
| Jonas Santos | RM563234 | Domínio, regras de negócio, API REST, testes |
| Gabriel Muller | RM561995 | Front-end Thymeleaf, UX, Docker, CI/CD e deploy |

---

## O desafio

A empresa parceira da FIAP precisava de uma solução tecnológica para **atendimentos veterinários**. Hoje, as informações de um pet ficam espalhadas: cadastro em uma planilha, agenda em outra ferramenta, prontuário em papel e dúvidas do tutor no WhatsApp. O resultado:

- A clínica não sabe quais pets estão sem acompanhamento há muito tempo.
- O histórico clínico se perde quando o pet troca de veterinário.
- Tutores esquecem consultas e não recebem retorno sobre suas dúvidas.
- Um pet costuma ter mais de um responsável (casal, família), e só um deles é avisado.

## A solução

O VetPet centraliza **pets, tutores, veterinários, atendimentos, prontuários e perguntas** e mantém a vida de cada pet **sempre monitorada**:

| Funcionalidade | Como resolve o problema |
|---|---|
| **Linha da vida do pet** | Todo evento relevante (cadastro, agendamento, cancelamento, prontuário, dúvida) é publicado no RabbitMQ e registrado em ordem cronológica na ficha do pet. |
| **Indicador de saúde** | Cada pet recebe um status: *Acompanhamento em dia*, *Check-up atrasado* (mais de 12 meses sem atendimento) ou *Sem atendimentos*. |
| **Vários tutores por pet** | Todos os responsáveis vinculados recebem as notificações. |
| **Atendimento → prontuário** | Um atendimento só é concluído quando o veterinário registra o prontuário. O peso aferido atualiza a ficha do pet. |
| **Lembretes automáticos** | Um agendador publica `exame.lembrete` 24 h antes de cada atendimento. |
| **Perguntas e respostas** | O tutor envia dúvidas pelo app (rota `/api/v1/me`) e a equipe responde pelo painel. A resposta gera uma notificação. |
| **Microsserviço de notificações** | Consome os eventos, garante idempotência e registra os avisos (e-mail simulado ou painel da clínica). |

**Originalidade:** em vez de um CRUD isolado, o sistema trata a saúde do pet como uma **história contínua orientada a eventos**. A mesma mensagem que alimenta a linha da vida também dispara a notificação ao tutor, sem acoplamento entre os serviços.

---

## Arquitetura

```mermaid
flowchart LR
    subgraph Usuarios[Usuários]
        Equipe[Equipe da clínica<br/>navegador]
        AppTutor[App do tutor<br/>front-end externo]
    end

    subgraph API[vetpet-api :8080]
        Web[Controllers Thymeleaf<br/>web.view]
        Rest[Controllers REST<br/>web.api /api/v1]
        Services[Services<br/>regras de negócio]
        Repo[Repositories<br/>Spring Data JPA]
        Pub[RabbitEventoDispatcher<br/>publisher após commit]
        HistListener[HistoricoPetListener<br/>consumer]
        Lembrete[LembreteScheduler]
    end

    subgraph Broker[RabbitMQ]
        EX{{vetpet.events<br/>topic exchange}}
        QH[(vetpet.historico)]
        QN[(vetpet.notificacoes)]
        DLX{{vetpet.events.dlx}}
        DLQ1[(vetpet.historico.dlq)]
        DLQ2[(vetpet.notificacoes.dlq)]
    end

    subgraph Notif[vetpet-notificacao :8081]
        NListener[NotificacaoListener]
        NService[NotificacaoService<br/>idempotente]
        NRest[GET /api/notificacoes<br/>X-API-KEY]
    end

    DB[(Oracle FIAP<br/>ou H2 local)]
    NDB[(H2 do serviço<br/>de notificação)]

    Equipe --> Web
    AppTutor --> Rest
    Web --> Services
    Rest --> Services
    Services --> Repo --> DB
    Services -- ApplicationEvent --> Pub
    Lembrete --> Services
    Pub -- routing key --> EX
    EX -- pet.# exame.# prontuario.# pergunta.# --> QH
    EX -- tutor.cadastrado pet.# exame.# prontuario.# pergunta.# --> QN
    QH --> HistListener --> Services
    QN --> NListener --> NService --> NDB
    QH -. falha após 3 tentativas .-> DLX
    QN -. falha após 3 tentativas .-> DLX
    DLX --> DLQ1
    DLX --> DLQ2
    Web -- RestClient --> NRest
```

### Camadas da API (`vetpet-api`)

```
src/main/java/br/com/fiap/vetpet
├── config          Configurações (RabbitMQ, OpenAPI, BCrypt, dados de demonstração)
├── domain
│   ├── entity      Entidades JPA (Pessoa, Tutor, Veterinario, Pet, Exame, Prontuario, Pergunta, EventoPet)
│   └── enums       Especie, Sexo, StatusExame, TipoAtendimento, TipoEvento, SituacaoSaude, Perfil
├── dto
│   ├── request     Entradas validadas com Bean Validation (usadas pela API e pelos formulários)
│   └── response    Saídas (nunca expõem entidades nem senhas)
├── exception       Exceções de negócio + handler RFC 7807 (ProblemDetail)
├── integration     Cliente HTTP do microsserviço de notificação
├── messaging       Evento de domínio, publisher, consumer e agendador de lembretes
├── repository      Spring Data JPA com consultas JPQL
├── security        Spring Security (duas cadeias: API stateless e painel com sessão)
├── service         Regras de negócio transacionais
└── web
    ├── api         Controllers REST versionados em /api/v1
    └── view        Controllers MVC das telas Thymeleaf
```

### Camadas do microsserviço (`vetpet-notificacao`)

```
src/main/java/br/com/fiap/vetpet/notificacao
├── config       RabbitMQ (exchange, fila, DLQ), propriedades e filtro da chave de API
├── domain       Notificacao e Canal (EMAIL, EMAIL_SIMULADO, EMAIL_FALHOU, PAINEL_CLINICA)
├── messaging    Contrato do evento e listener da fila vetpet.notificacoes
├── repository   Spring Data JPA
├── service      Processamento idempotente, envio de e-mail e template HTML
└── web          GET /api/notificacoes
```

---

## Mensageria com RabbitMQ

### Topologia

| Elemento | Tipo | Função |
|---|---|---|
| `vetpet.events` | Topic exchange | Recebe todos os eventos de domínio |
| `vetpet.historico` | Fila (API) | Bindings `pet.#`, `exame.#`, `prontuario.#`, `pergunta.#`. Alimenta a linha da vida |
| `vetpet.notificacoes` | Fila (microsserviço) | Bindings `tutor.cadastrado`, `pet.#`, `exame.#`, `prontuario.#`, `pergunta.#` |
| `vetpet.events.dlx` | Direct exchange | Dead-letter exchange |
| `*.dlq` | Filas | Recebem mensagens que falharam 3 vezes (retry com backoff) |

### Eventos publicados

| Routing key | Quando acontece |
|---|---|
| `tutor.cadastrado` | Novo tutor (boas-vindas) |
| `pet.cadastrado` / `pet.atualizado` | Cadastro ou edição do pet |
| `pet.tutor-vinculado` | Novo responsável vinculado ao pet |
| `exame.agendado` | Atendimento agendado |
| `exame.status-alterado` / `exame.cancelado` | Atendimento adiado, reconfirmado ou cancelado |
| `exame.lembrete` | 24 h antes do atendimento (agendador) |
| `prontuario.registrado` | Atendimento concluído com prontuário |
| `pergunta.criada` / `pergunta.respondida` | Dúvida do tutor e resposta da equipe |

### Contrato da mensagem (JSON)

```json
{
  "id": "3f0d6c1e-7a4b-4c55-9a0e-1c2b9d7f8e10",
  "tipo": "EXAME_AGENDADO",
  "petId": 1,
  "petNome": "Thor",
  "destinatarios": ["mariana@example.com", "rafael@example.com"],
  "titulo": "Atendimento agendado",
  "descricao": "Consulta de Thor agendado(a) para 20/10/2026 às 10:00 com Ana Ribeiro.",
  "ocorridoEm": "2026-10-15T19:40:12"
}
```

### Fluxo de um agendamento

```mermaid
sequenceDiagram
    actor Vet as Veterinária
    participant API as vetpet-api
    participant DB as Banco
    participant MQ as RabbitMQ
    participant N as vetpet-notificacao

    Vet->>API: POST /atendimentos/novo
    API->>DB: salva Exame (transação)
    API-->>API: publica EventoVetPet (ApplicationEvent)
    Note over API: @TransactionalEventListener(AFTER_COMMIT)<br/>só envia se o banco confirmou
    API->>MQ: exame.agendado → vetpet.events
    MQ->>API: fila vetpet.historico
    API->>DB: grava EventoPet (linha da vida)
    MQ->>N: fila vetpet.notificacoes
    N->>N: ignora se o id já foi processado
    N-->>N: registra notificação e "envia" e-mail
```

### Decisões de projeto

- **Publicação após o commit:** o evento só vai para o broker se a transação foi confirmada. Assim não existe aviso de um agendamento que foi desfeito.
- **Resiliência:** se o RabbitMQ estiver fora do ar, a API não falha. O evento é registrado diretamente na linha da vida e o erro vai para o log.
- **Idempotência:** os dois consumidores usam o `id` do evento como chave única. Mensagens reentregues não geram duplicidade.
- **Retry + DLQ:** 3 tentativas com intervalo de 1 s. Depois disso a mensagem vai para a DLQ, sem travar a fila.
- **JSON com Jackson 3** (`JacksonJsonMessageConverter`): o contrato é independente de linguagem, o que facilita criar novos consumidores (ex.: app mobile, serviço de SMS).
- **E-mail real com fallback:** o microsserviço envia e-mails HTML pelo SMTP do Gmail. Sem credenciais configuradas, ou se o SMTP falhar, a notificação é registrada como *simulada* ou *falha* e a fila continua andando.
- **Banco por serviço:** o microsserviço de notificação tem seu próprio banco, como recomendado em microsserviços.

### Avisos por e-mail aos tutores

Todos os tutores vinculados ao pet recebem um e-mail com a identidade visual do VetPet quando:

| Situação | Evento |
|---|---|
| Cadastro do tutor (boas-vindas) | `tutor.cadastrado` |
| Pet cadastrado no acompanhamento | `pet.cadastrado` |
| Dados do pet atualizados | `pet.atualizado` |
| Tutor vinculado ao pet com sucesso | `pet.tutor-vinculado` |
| Consulta ou exame agendado, adiado, reconfirmado ou cancelado | `exame.agendado`, `exame.status-alterado`, `exame.cancelado` |
| Lembrete 24 h antes do atendimento | `exame.lembrete` |
| Prontuário registrado (diagnóstico e retorno) | `prontuario.registrado` |
| Resposta da clínica a uma dúvida | `pergunta.respondida` |

**Como ativar o envio real (Gmail):**

1. Na conta Google que vai enviar os e-mails, ative a **verificação em duas etapas**.
2. Acesse **Conta Google → Segurança → Senhas de app**, crie uma senha para "VetPet" e copie os 16 caracteres.
3. Copie `vetpet-notificacao/local.properties.example` para `vetpet-notificacao/local.properties` e preencha `MAIL_USERNAME` (o Gmail) e `MAIL_PASSWORD` (a senha de app, sem espaços). Na Azure, essas variáveis ficam protegidas no grupo de variáveis da pipeline (veja [Deploy](#deploy-cloud--devops)).

Sem essas variáveis o envio fica **simulado** (aparece no log e na tela de notificações). E-mails de demonstração com domínio `example.com` nunca são enviados.

---

## Tecnologias e justificativas

| Tecnologia | Uso | Por que escolhemos |
|---|---|---|
| Java 21 | Linguagem | LTS, records, `getFirst()`, pattern matching |
| Spring Boot 4.0 | Base dos dois serviços | Padrão de mercado, autoconfiguração e actuator |
| Spring Data JPA + Hibernate | Persistência | Repositórios declarativos e consultas JPQL com filtros opcionais |
| Oracle (FIAP) / H2 | Banco | Oracle como banco oficial (perfil `oracle`). H2 em arquivo para desenvolvimento local e em memória nos testes |
| Spring Security + BCrypt | Autenticação | Senhas com hash, perfis ADMIN/VETERINARIO/TUTOR, CSRF nos formulários |
| Spring AMQP + RabbitMQ | Mensageria | Desacopla a API do serviço de notificação e prepara a arquitetura de microsserviços |
| Thymeleaf | Painel da clínica | Renderização no servidor, integração nativa com Spring MVC e validação |
| Bean Validation | Validação | As mesmas regras valem para a API e para os formulários |
| springdoc-openapi | Swagger UI | Documentação viva e testável da API |
| Lombok | Menos código repetitivo | Getters, setters e construtores nos DTOs e entidades |
| JUnit 5 + Mockito + MockMvc | Testes | Testes de unidade nos services e de integração em API e telas |
| Docker + Docker Compose | Containers | Mesmo ambiente local e em nuvem. Sobe API, notificação e RabbitMQ com um comando |
| Azure DevOps (Azure Pipelines) | CI/CD | Compila, testa e publica o artefato de cada serviço a cada push na branch `master` |
| Azure App Service + Azure SQL | Deploy e banco em nuvem | Plataforma gerenciada (PaaS) para Java 21 e banco SQL Server gerenciado, sem administrar servidores |
| CloudAMQP | RabbitMQ gerenciado | Plano gratuito, acessível pelos dois Web Apps |

### Identidade visual

- **Cores:** berinjela (`#24123A`) e violeta (`#5B2A86`) mantêm a identidade roxa das sprints anteriores. Verde-menta indica "saúde em dia", âmbar indica "atenção" e coral indica cancelamento.
- **Tipografia:** *Bricolage Grotesque* nos títulos e *Atkinson Hyperlegible* no texto, uma fonte criada para máxima legibilidade, importante em ambiente clínico.
- **Elemento central:** o indicador de saúde com "batimento" e a linha da vida colorida por categoria (agenda em azul, prontuário em verde, perguntas em âmbar).
- **Acessibilidade:** foco visível, navegação por teclado nas abas, `prefers-reduced-motion` respeitado, rótulos em todos os campos e layout responsivo com menu lateral recolhível.

---

## Modelo de dados

```mermaid
erDiagram
    VP_CREDENCIAL ||--o| VP_TUTOR : autentica
    VP_CREDENCIAL ||--o| VP_VETERINARIO : autentica
    VP_TUTOR }o--o{ VP_PET : "VP_TUTOR_PET"
    VP_PET ||--o{ VP_EXAME : possui
    VP_VETERINARIO ||--o{ VP_EXAME : realiza
    VP_EXAME ||--o| VP_PRONTUARIO : gera
    VP_PET ||--o{ VP_PERGUNTA : recebe
    VP_PET ||--o{ VP_EVENTO_PET : "linha da vida"

    VP_PET {
        number id PK
        varchar pet_code UK
        varchar nome
        date data_nascimento
        varchar especie
        number peso_kg
    }
    VP_EXAME {
        number id PK
        timestamp data_hora
        varchar tipo
        varchar status
        number pet_id FK
        number veterinario_id FK
    }
    VP_PRONTUARIO {
        number id PK
        number exame_id FK
        varchar diagnostico
        date proximo_retorno
    }
    VP_EVENTO_PET {
        number id PK
        varchar evento_id UK
        varchar tipo
        timestamp ocorrido_em
    }
```

- `Tutor` e `Veterinario` herdam de `Pessoa` (`@MappedSuperclass`). O endereço é um `@Embeddable`.
- A idade do pet é **calculada** a partir da data de nascimento, nunca armazenada (evita dado desatualizado).
- Scripts SQL de evidência: [`docs/sql/01-schema-oracle.sql`](docs/sql/01-schema-oracle.sql) (DDL) e [`docs/sql/02-consultas-gestao.sql`](docs/sql/02-consultas-gestao.sql) (pets com check-up atrasado, agenda do dia por veterinário, taxa de cancelamento por tipo, evolução de peso e tempo médio de resposta às dúvidas).

---

## Telas (Thymeleaf)

| Rota | Tela |
|---|---|
| `/` | Página inicial com a proposta do produto |
| `/login` | Login da equipe |
| `/dashboard` | Indicadores do dia, próximos atendimentos e dúvidas sem resposta |
| `/pets` | Lista com busca e paginação |
| `/pets/{id}` | **Ficha do pet**: indicador de saúde, linha da vida, atendimentos, prontuários, perguntas e tutores |
| `/pets/novo`, `/pets/{id}/editar` | Cadastro e edição com escolha de vários tutores |
| `/tutores`, `/tutores/{id}` | Lista e detalhe do tutor com seus pets |
| `/veterinarios` | Equipe (cadastro e edição apenas para ADMIN) |
| `/atendimentos` | Agenda com filtros por status, veterinário e data, além das ações adiar, confirmar e cancelar |
| `/atendimentos/novo` | Agendamento |
| `/prontuarios`, `/prontuarios/novo?exameId=` | Registro clínico que conclui o atendimento |
| `/notificacoes` | Avisos gerados pelo microsserviço (consulta via REST) |

**Decisões de UX**

- Um único layout com fragments (`fragments/layout.html`) e componentes de formulário reutilizáveis (`entrada`, `texto`, `dadosPessoais`), sem CSS ou HTML repetido entre páginas.
- Erros de validação aparecem no próprio campo e o formulário mantém o que foi digitado.
- Ações destrutivas pedem confirmação. Mensagens de sucesso usam o mesmo verbo do botão.
- Estados vazios explicam o próximo passo ("Agende a primeira consulta").

---

## API REST

Base: `/api/v1`. Autenticação **HTTP Basic** (stateless). Erros no padrão **RFC 7807 (ProblemDetail)**. Documentação interativa em `/swagger-ui.html`.

| Recurso | Endpoints principais | Acesso |
|---|---|---|
| Autenticação | `POST /auth/tutores/login`, `POST /auth/veterinarios/login` | Público |
| Tutores | `GET /tutores?busca=&pagina=&tamanho=`, `GET /tutores/{id}`, `GET /tutores/cpf/{cpf}`, `GET /tutores/email/{email}`, `POST /tutores` (público), `PUT /tutores/{id}`, `DELETE /tutores/{id}` | ADMIN, VETERINARIO |
| Veterinários | `GET /veterinarios`, `GET /veterinarios/{id}`, `GET /veterinarios/crmv/{crmv}`, `POST`, `PUT`, `DELETE` | Leitura: equipe. Escrita: ADMIN |
| Pets | `GET /pets`, `GET /pets/{id}`, `GET /pets/codigo/{petCode}`, `GET /pets/{id}/acompanhamento`, `GET /pets/{id}/linha-do-tempo`, `POST /pets`, `PUT /pets/{id}`, `POST/DELETE /pets/{id}/tutores/{tutorId}`, `DELETE /pets/{id}` | ADMIN, VETERINARIO |
| Atendimentos | `GET /exames?status=&petId=&veterinarioId=&data=`, `GET /exames/{id}`, `GET /exames/proximo?petId=`, `POST /exames`, `PATCH /exames/{id}/status`, `PATCH /exames/{id}/cancelamento` | ADMIN, VETERINARIO |
| Prontuários | `GET /prontuarios?petId=&veterinarioId=`, `GET /prontuarios/{id}`, `POST /prontuarios`, `PUT /prontuarios/{id}` | ADMIN, VETERINARIO |
| Perguntas | `GET/POST /pets/{petId}/perguntas`, `PATCH /perguntas/{id}/resposta`, `DELETE /perguntas/{id}` | ADMIN, VETERINARIO |
| Portal do tutor | `GET /me`, `GET /me/pets`, `GET /me/pets/{id}/acompanhamento`, `GET /me/exames`, `POST /me/pets/{id}/perguntas`, `PATCH /me/senha` | TUTOR (só os próprios pets) |
| Dashboard | `GET /dashboard` | ADMIN, VETERINARIO |

Exemplo de cadastro de pet:

```bash
curl -u admin@vetpet.com:admin123 -X POST http://localhost:8080/api/v1/pets \
  -H "Content-Type: application/json" \
  -d '{"nome":"Mel","dataNascimento":"2022-03-10","especie":"GATO","raca":"Persa","sexo":"FEMEA","pesoKg":3.8,"tutoresIds":[1]}'
```

Exemplo de erro de validação:

```json
{
  "type": "about:blank",
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Um ou mais campos não passaram na validação.",
  "erros": { "nome": "Informe o nome do pet", "tutoresIds": "Selecione ao menos um tutor" }
}
```

---

## Segurança

- **Duas cadeias de filtros:** `/api/**` é stateless, com HTTP Basic, sem CSRF e com CORS configurável (`CORS_ORIGENS`). O painel usa sessão, formulário de login e CSRF.
- **Senhas com BCrypt.** Nenhuma resposta da API retorna senha ou hash.
- **Perfis:** `ADMIN` (configurado por variável de ambiente), `VETERINARIO` (cadastrados na equipe) e `TUTOR` (app do tutor, acesso apenas aos próprios pets).
- **Sem credenciais no código:** banco, RabbitMQ, admin e chave da API de notificação vêm de variáveis de ambiente ou dos arquivos `local.properties` de cada aplicação (veja os `local.properties.example`).
- O microsserviço de notificação exige o cabeçalho `X-API-KEY`, comparado em tempo constante.

---

## Banco de dados

A API funciona com dois bancos, escolhidos pelo perfil do Spring, sem alterar código:

| Perfil | Banco | Quando usar |
|---|---|---|
| *(padrão)* | **H2 em arquivo** (`vetpet-api/data/`) | Desenvolvimento local. Não precisa de instalação nem credenciais, e os dados ficam salvos entre as execuções |
| `oracle` | **Oracle FIAP** | Banco oficial do projeto e da disciplina de banco de dados |
| `prod` | **Azure SQL** (definido por `DB_URL`) | Deploy na Azure |

As tabelas (prefixo `VP_`) são criadas e atualizadas pelo Hibernate (`ddl-auto=update`). Para usar o Oracle no IntelliJ, abra *Run → Edit Configurations*, preencha **Active profiles** com `oracle` e defina `DB_USERNAME` e `DB_PASSWORD` nas variáveis de ambiente (ou no `vetpet-api/local.properties`). Para apagar os dados locais, basta excluir a pasta `vetpet-api/data/`.

O código não depende de recursos específicos de um banco: só usa tipos portáveis (`VARCHAR`, `DECIMAL`, `DATE`, `TIMESTAMP`), chaves `IDENTITY` e consultas JPQL. O Hibernate detecta o dialeto pela URL, e os drivers do Oracle, do SQL Server e do H2 já estão no `pom.xml`, então **trocar de banco não exige alterar código**.

| Banco | `DB_URL` |
|---|---|
| Oracle FIAP | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` |
| Azure SQL | `jdbc:sqlserver://sql-vetpet-rm565824.database.windows.net:1433;database=vetpet;encrypt=true;trustServerCertificate=false;loginTimeout=30` |

Na nuvem, as duas aplicações usam o **Azure SQL**: a API grava as tabelas do domínio e o microsserviço grava o histórico de notificações (`VP_NOTIFICACAO`). O DDL equivalente está em [`docs/sql/03-schema-sqlserver.sql`](docs/sql/03-schema-sqlserver.sql).

> O Oracle da FIAP limita as sessões simultâneas por aluno (`ORA-02391`). Por isso o pool usa no máximo 2 conexões (`DB_POOL_MAX`), e cada integrante deve usar o próprio RM.

Os testes automatizados usam H2 em memória, então nunca mexem no banco real.

---

## Como executar

Pré-requisitos: **JDK 21**, **Docker Desktop** e **IntelliJ** (ou Maven).

### 1. Subir a infraestrutura

Na raiz do repositório:

```bash
docker compose up -d
```

Isso sobe o **RabbitMQ** com painel em http://localhost:15672 (usuário `vetpet`, senha `vetpet`). As filas, exchanges e bindings são criados automaticamente pelas aplicações quando elas iniciam.

### 2. Configurar os arquivos locais

Cada aplicação lê um `local.properties` na própria pasta. Esses arquivos estão no `.gitignore` e nunca vão para o GitHub.

| Arquivo | Para quê |
|---|---|
| `vetpet-api/local.properties` | Usuário e senha do Oracle, usados com o perfil `oracle` (copie de `local.properties.example`). Também pode usar as variáveis `DB_USERNAME` e `DB_PASSWORD` no IntelliJ. Sem perfil, a API usa H2 e não precisa deste arquivo |
| `vetpet-notificacao/local.properties` | Gmail e senha de app para o envio real de e-mails (copie de `local.properties.example`). Sem ele, o envio fica simulado |

### 3. Iniciar os serviços

Abra dois terminais na raiz (ou rode cada `Application` pelo IntelliJ).

Terminal 1, serviço de notificação:

```bash
cd vetpet-notificacao
./mvnw spring-boot:run
```

Terminal 2, API e painel:

```bash
cd vetpet-api
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd`.

| Serviço | Endereço |
|---|---|
| Painel da clínica | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |
| Serviço de notificação | http://localhost:8081/api/notificacoes (cabeçalho `X-API-KEY: vetpet-dev-key`) |
| RabbitMQ Management | http://localhost:15672 |

### 4. Testar a comunicação entre os serviços

1. Entre no painel com `admin@vetpet.com` / `admin123`.
2. Abra o pet **Thor** e agende um atendimento.
3. No RabbitMQ Management, em **Queues**, veja as mensagens passando por `vetpet.historico` e `vetpet.notificacoes`.
4. Na ficha do Thor, o evento aparece na **linha da vida**.
5. Na tela **Notificações**, o aviso aparece como "E-mail enviado", e o e-mail chega para os tutores do Thor.

Para rodar a API **sem RabbitMQ**, defina `MENSAGERIA_HABILITADA=false`. A linha da vida continua funcionando, pois os eventos são registrados diretamente.

### Acessos de demonstração

| Perfil | E-mail | Senha |
|---|---|---|
| Administrador | admin@vetpet.com | admin123 |
| Veterinária | ana@vetpet.com | vet12345 |
| Veterinário | carlos@vetpet.com | vet12345 |
| Tutora (API `/api/v1/me`) | mariana@example.com | tutor123 |
| Tutor com e-mail real (recebe os avisos) | pedroferreira.gomes03@gmail.com | tutor123 |

### Variáveis de ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | Oracle FIAP | URL JDBC (Oracle ou Azure SQL), usada nos perfis `oracle` e `prod` |
| `DB_USERNAME`, `DB_PASSWORD` | - | Credenciais do banco |
| `DDL_AUTO` | `update` | Estratégia do Hibernate para o esquema |
| `DB_POOL_MAX` | `2` | Conexões simultâneas com o banco (o Oracle da FIAP limita as sessões por usuário) |
| `VETPET_ADMIN_EMAIL`, `VETPET_ADMIN_SENHA` | admin@vetpet.com / admin123 | Administrador. **Obrigatórias em `prod`** |
| `RABBITMQ_URL` | `amqp://vetpet:vetpet@localhost:5672` | Aceita `amqps://` (CloudAMQP) |
| `MENSAGERIA_HABILITADA` | `true` | Liga ou desliga o RabbitMQ |
| `NOTIFICACAO_URL` | `http://localhost:8081` | Endereço do microsserviço |
| `NOTIFICACAO_API_KEY` | `vetpet-dev-key` | Mesma chave nos dois serviços |
| `SEED_HABILITADO` | `true` (local) / `false` (prod) | Cria dados de demonstração se o banco estiver vazio |
| `CORS_ORIGENS` | `*` | Origens permitidas para o front-end externo |
| `LEMBRETES_CRON` | `0 */30 * * * *` | Frequência do agendador de lembretes |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | (vazio = e-mail simulado) | Gmail e senha de app (no `vetpet-notificacao`) |

---

## Deploy (Cloud / DevOps)

A solução roda na **Azure** com serviços gerenciados (PaaS), e o CI/CD é feito no **Azure DevOps**.

![Arquitetura da solução e esteira CI/CD](docs/img/arquitetura-vetpet.png)

> Os números em vermelho mostram a ordem da esteira CI/CD, do commit até o deploy.

| Recurso | Nome | Função |
|---|---|---|
| Grupo de recursos | `rg-vetpet` | Agrupa todos os recursos |
| Azure SQL (servidor + banco) | `sql-vetpet-rm565824` / `vetpet` | Banco relacional em nuvem (PaaS) |
| Plano do App Service | `plan-vetpet` (Linux, B1) | Capacidade compartilhada pelos dois Web Apps |
| Web App | `vetpet-api-rm565824` | API e painel da clínica (Java 21) |
| Web App | `vetpet-notificacao-rm565824` | Microsserviço de notificação (Java 21) |
| CloudAMQP | `vetpet` | RabbitMQ gerenciado, usado pelos dois Web Apps |

```mermaid
flowchart LR
    Dev[1. Push na branch master] --> CI[2. CI: build + testes]
    CI --> Art[3. Artefatos app.jar publicados]
    Art --> CD[4. CD: deploy automático]
    CD --> N[vetpet-notificacao<br/>Azure Web App]
    CD --> A[vetpet-api<br/>Azure Web App]
    A --> SQL[(Azure SQL)]
    N --> SQL
    A <--> MQ[(CloudAMQP)]
    N <--> MQ
    N --> Gmail[Gmail SMTP]
```

### Pipeline ([`azure-pipelines.yml`](azure-pipelines.yml))

| Estágio | Etapa | O que faz |
|---|---|---|
| **CI** | Compilar e executar testes | `./mvnw clean verify` em cada aplicação (job em matriz) |
| | Publicar resultados dos testes | Relatórios JUnit na aba *Tests* da execução |
| | Preparar e publicar artefato | `app.jar` de cada aplicação publicado no Azure DevOps |
| **CD** | Baixar artefato | Usa o artefato gerado pelo CI da mesma execução |
| | Publicar no Web App | `AzureWebApp@1` publica o `.jar` e aplica as configurações |

- O CI dispara a cada alteração na branch **`master`**. O CD só roda depois que o CI gera os artefatos com sucesso.
- O microsserviço de notificação é publicado antes da API, que depende dele.
- Usuários, senhas e tokens ficam como **variáveis secretas** no grupo `vetpet-segredos` do Azure DevOps. Nada sensível fica no repositório.

### Como reproduzir

O passo a passo completo, do portal da Azure até a primeira execução da pipeline, está em [`docs/azure/PASSO-A-PASSO.md`](docs/azure/PASSO-A-PASSO.md). Em resumo:

1. No **Azure Cloud Shell** (Bash), crie os recursos com o script [`scripts/azure/criar-recursos.sh`](scripts/azure/criar-recursos.sh).
2. Crie a instância gratuita do RabbitMQ no CloudAMQP.
3. No Azure DevOps, crie a conexão com a assinatura (`vetpet-azure`), o grupo de variáveis `vetpet-segredos` e a pipeline a partir do `azure-pipelines.yml`.
4. Faça um push na `master` e acompanhe o CI e o CD.

Para remover os recursos: [`scripts/azure/excluir-recursos.sh`](scripts/azure/excluir-recursos.sh).

### Práticas de DevOps aplicadas

- CI/CD com estágios separados, artefato versionado por execução e testes obrigatórios antes do deploy.
- Configuração externa por variáveis de ambiente (12-factor) e segredos protegidos na pipeline.
- Infraestrutura criada por **Azure CLI** (scripts versionados).
- Actuator com *liveness/readiness probes* (`/actuator/health/liveness`).
- Dockerfile **multi-stage** e `docker-compose` para o ambiente local.

---

## Testes e qualidade

Em cada pasta:

```bash
./mvnw clean verify
```

| Projeto | Teste | O que cobre |
|---|---|---|
| vetpet-api | `TutorServiceTest` | BCrypt, normalização de e-mail e CPF, e-mail e CPF duplicados |
| vetpet-api | `PetServiceTest` | Vínculo de vários tutores, tutor inexistente, último tutor não pode ser removido, cálculo de idade |
| vetpet-api | `ExameServiceTest` | Agendamento publica `exame.agendado`, conflito de horário, conclusão só com prontuário, cancelamento |
| vetpet-api | `ProntuarioServiceTest` | Conclusão do atendimento, atualização do peso, bloqueio de cancelados e duplicados |
| vetpet-api | `AcompanhamentoServiceTest` | Regra do indicador de saúde |
| vetpet-api | `VetPetIntegracaoTest` | Segurança (401/403), validação da API, fluxo completo até a linha da vida e **renderização de todas as telas** |
| vetpet-notificacao | `NotificacaoServiceTest` | Canal correto, destinatários, painel da clínica e idempotência |
| vetpet-notificacao | `EmailServiceTest` | Envio simulado sem credenciais, domínios ignorados e falha de SMTP |
| vetpet-notificacao | `NotificacaoApplicationTest` | Chave de API e filtro por e-mail |

Os testes usam H2 e mensageria desligada, então rodam sem Oracle e sem RabbitMQ (inclusive na pipeline do Azure DevOps).

---

## Integração multidisciplinar

| Disciplina | Como foi aplicada | Evidências |
|---|---|---|
| **Java Advanced** | Spring Boot em camadas, JPA, Bean Validation, Security, Thymeleaf, RabbitMQ, testes | Este repositório, Swagger, testes |
| **Cloud / DevOps** | Pipeline CI/CD no Azure DevOps, deploy no Azure App Service, banco Azure SQL, recursos criados por Azure CLI, segredos protegidos, RabbitMQ gerenciado (CloudAMQP) | `azure-pipelines.yml`, `scripts/azure/`, `docs/azure/`, diagrama em `docs/img/` |
| **Banco de dados** | Modelo relacional normalizado, DDL para Oracle e Azure SQL e consultas gerenciais | `docs/sql/` |

---

## Boas práticas aplicadas

- **SOLID e camadas bem definidas:** controllers só orquestram, services concentram as regras e repositories só acessam dados.
- **DTOs** em todas as entradas e saídas. Entidades nunca são expostas.
- **Sem repetição:** `Pessoa` como superclasse, `Endereco` embutido, `DadosPessoais` para a cópia de dados, `Formularios` para o fluxo padrão das telas e fragments Thymeleaf para layout e campos.
- **Tratamento de erros centralizado** com exceções semânticas (`RecursoNaoEncontrado` 404, `DadoDuplicado` 409, `RegraNegocio` 422).
- **Transações** declarativas (`readOnly` nas consultas) e `open-in-view` desligado.
- **API versionada** (`/api/v1`), com paginação e verbos HTTP corretos (`201 Created` com `Location`, `204 No Content`).
- **Código limpo:** nomes em português alinhados ao domínio e sem comentários, já que o código se explica.

---

## Evolução desde as sprints anteriores

| Antes | Agora |
|---|---|
| Senhas em texto puro e senha do veterinário retornada no JSON | BCrypt e DTOs de resposta sem dados sensíveis |
| Credenciais do Oracle no `application.properties` | Variáveis de ambiente e `local.properties` fora do Git |
| `PUT /tutores/alterar-senha/{email}-{novaSenha}` (senha na URL) | `PATCH /api/v1/me/senha` com corpo JSON |
| `VARCHAR(MAX)` (SQL Server) em projeto Oracle | Tipos compatíveis com Oracle e H2 |
| Login do tutor com erro 500 quando o e-mail não existia | `401` com mensagem padronizada |
| Tutor sem endereço causava erro 500 | Endereço opcional (`@Embeddable`) |
| Pet com vários tutores vinculava apenas o primeiro | Todos os tutores vinculados e notificados |
| Rotas como `/pets/buscar/pet-code-` | Rotas REST versionadas (`/api/v1/pets/codigo/{petCode}`) |
| CSS copiado em cada página, telas só para pet, tutor e veterinário | Layout único, design system próprio e telas para todo o fluxo |
| Sem mensageria | RabbitMQ com exchange topic, DLQ, retry, idempotência e microsserviço |
| Um teste que dependia do Oracle | Testes de unidade e integração independentes de infraestrutura |

> **Atenção para o front-end externo:** as rotas da API mudaram para `/api/v1/...`. Consulte o Swagger para os novos caminhos.

---

## Organização do repositório

```
.
├── vetpet-api/                    API REST + painel Thymeleaf (porta 8080)
│   ├── src/
│   ├── Dockerfile
│   ├── local.properties.example   Modelo da conexão com o Oracle (perfil oracle)
│   └── pom.xml
├── vetpet-notificacao/            Microsserviço de notificação por e-mail (porta 8081)
│   ├── src/
│   ├── Dockerfile
│   ├── local.properties.example   Modelo do Gmail e senha de app
│   └── pom.xml
├── scripts/azure/                 Criação dos recursos na Azure (Azure CLI)
├── docs/
│   ├── azure/                     Passo a passo do Azure DevOps e da Azure
│   ├── sql/                       DDL Oracle, DDL SQL Server (Azure) e consultas gerenciais
│   └── img/                       Imagens de apoio
├── azure-pipelines.yml           Pipeline CI/CD do Azure DevOps (as duas aplicações)
├── docker-compose.yml             Infraestrutura local (RabbitMQ)
└── README.md
```

Dependências escolhidas no Spring Initializr nas sprints iniciais:

![Dependências do projeto](docs/img/dependencias-spring-initializr.png)
