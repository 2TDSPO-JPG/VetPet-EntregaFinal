CREATE TABLE VP_CREDENCIAL (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    senha_hash  VARCHAR(100) NOT NULL
);

CREATE TABLE VP_TUTOR (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    nome           VARCHAR(120) NOT NULL,
    cpf            VARCHAR(14),
    email          VARCHAR(120) NOT NULL UNIQUE,
    telefone       VARCHAR(20),
    foto_url       VARCHAR(500),
    cep            VARCHAR(9),
    logradouro     VARCHAR(150),
    numero         VARCHAR(10),
    complemento    VARCHAR(80),
    bairro         VARCHAR(80),
    cidade         VARCHAR(80),
    estado         VARCHAR(2),
    credencial_id  BIGINT UNIQUE REFERENCES VP_CREDENCIAL (id),
    criado_em      DATETIME2 NOT NULL,
    ultimo_acesso  DATETIME2
);

CREATE TABLE VP_VETERINARIO (
    id                BIGINT IDENTITY(1,1) PRIMARY KEY,
    nome              VARCHAR(120) NOT NULL,
    cpf               VARCHAR(14),
    email             VARCHAR(120) NOT NULL UNIQUE,
    telefone          VARCHAR(20),
    foto_url          VARCHAR(500),
    cep               VARCHAR(9),
    logradouro        VARCHAR(150),
    numero            VARCHAR(10),
    complemento       VARCHAR(80),
    bairro            VARCHAR(80),
    cidade            VARCHAR(80),
    estado            VARCHAR(2),
    credencial_id     BIGINT UNIQUE REFERENCES VP_CREDENCIAL (id),
    crmv              VARCHAR(20) NOT NULL UNIQUE,
    especialidade     VARCHAR(80),
    anos_experiencia  INT,
    biografia         VARCHAR(1000),
    criado_em         DATETIME2 NOT NULL,
    ultimo_acesso     DATETIME2
);

CREATE TABLE VP_PET (
    id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    pet_code         VARCHAR(12) NOT NULL UNIQUE,
    nome             VARCHAR(80) NOT NULL,
    data_nascimento  DATE NOT NULL,
    especie          VARCHAR(20) NOT NULL,
    raca             VARCHAR(80),
    sexo             VARCHAR(10),
    peso_kg          DECIMAL(6, 2),
    alergias         VARCHAR(500),
    criado_em        DATETIME2 NOT NULL
);

CREATE TABLE VP_TUTOR_PET (
    pet_id    BIGINT NOT NULL REFERENCES VP_PET (id),
    tutor_id  BIGINT NOT NULL REFERENCES VP_TUTOR (id),
    PRIMARY KEY (pet_id, tutor_id)
);

CREATE TABLE VP_EXAME (
    id                BIGINT IDENTITY(1,1) PRIMARY KEY,
    data_hora         DATETIME2 NOT NULL,
    tipo              VARCHAR(30) NOT NULL,
    motivo            VARCHAR(500),
    status            VARCHAR(20) NOT NULL,
    lembrete_enviado  BIT NOT NULL DEFAULT 0,
    criado_em         DATETIME2 NOT NULL,
    pet_id            BIGINT NOT NULL REFERENCES VP_PET (id),
    veterinario_id    BIGINT NOT NULL REFERENCES VP_VETERINARIO (id)
);

CREATE TABLE VP_PRONTUARIO (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    exame_id            BIGINT NOT NULL UNIQUE REFERENCES VP_EXAME (id),
    diagnostico         VARCHAR(2000) NOT NULL,
    prescricao          VARCHAR(2000),
    exames_solicitados  VARCHAR(1000),
    observacoes         VARCHAR(2000),
    peso_aferido_kg     DECIMAL(6, 2),
    proximo_retorno     DATE,
    registrado_em       DATETIME2 NOT NULL,
    atualizado_em       DATETIME2
);

CREATE TABLE VP_PERGUNTA (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    pet_id          BIGINT NOT NULL REFERENCES VP_PET (id),
    texto           VARCHAR(1000) NOT NULL,
    autor           VARCHAR(120),
    resposta        VARCHAR(2000),
    respondida_por  VARCHAR(120),
    criada_em       DATETIME2 NOT NULL,
    respondida_em   DATETIME2
);

CREATE TABLE VP_EVENTO_PET (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    evento_id    VARCHAR(36) NOT NULL UNIQUE,
    pet_id       BIGINT NOT NULL REFERENCES VP_PET (id),
    tipo         VARCHAR(40) NOT NULL,
    titulo       VARCHAR(150) NOT NULL,
    descricao    VARCHAR(1000),
    ocorrido_em  DATETIME2 NOT NULL
);

CREATE INDEX IX_VP_EXAME_PET_DATA ON VP_EXAME (pet_id, data_hora);
CREATE INDEX IX_VP_EXAME_VET_DATA ON VP_EXAME (veterinario_id, data_hora);
CREATE INDEX IX_VP_EVENTO_PET_DATA ON VP_EVENTO_PET (pet_id, ocorrido_em);
