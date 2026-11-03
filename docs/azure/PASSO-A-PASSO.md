# Passo a passo: Azure + Azure DevOps

Como colocar o VetPet no ar com CI/CD, do zero. As etapas seguem a ordem em que dependem uma da outra.

| Etapa | Onde |
|---|---|
| 0. Liberar a execução gratuita de pipelines | Formulário da Microsoft |
| 1. Criar os recursos na Azure | Azure Cloud Shell |
| 2. Criar o RabbitMQ | CloudAMQP |
| 3. Criar o projeto no Azure DevOps | dev.azure.com |
| 4. Conectar a Azure ao Azure DevOps | Project settings |
| 5. Cadastrar os segredos | Pipelines → Library |
| 6. Criar a pipeline | Pipelines |
| 7. Conferir o CRUD no banco | Portal da Azure |

---|---|---|
| 0. Pedir execução gratuita de pipelines | Formulário da Microsoft | 5 min (aprovação em até 3 dias úteis) |
| 1. Criar os recursos na Azure | Portal da Azure (Cloud Shell) | 15 min |
| 2. Criar o RabbitMQ | CloudAMQP | 5 min |
| 3. Criar o projeto no Azure DevOps | dev.azure.com | 10 min |
| 4. Conectar a Azure ao Azure DevOps | Project settings | 5 min |
| 5. Guardar os segredos | Pipelines → Library | 10 min |
| 6. Criar a pipeline | Pipelines | 10 min |
| 7. Testar e conferir o banco | Portal da Azure | 15 min |

---

## 0. Liberar a execução gratuita de pipelines

Organizações novas do Azure DevOps não podem executar pipelines nos agentes da Microsoft até a liberação gratuita ser aprovada.

1. Crie a organização no passo 3.1 primeiro (só a organização).
2. Acesse https://aka.ms/azpipelines-parallelism-request e preencha com o nome da organização, escolhendo **Private projects**.
3. A aprovação chega por e-mail em até 3 dias úteis. Enquanto isso, faça as outras etapas.

Para confirmar: *Organization settings → Pipelines → Parallel jobs*. Em **Microsoft-hosted**, deve aparecer **1** em *Private projects*.

---

## 1. Criar os recursos na Azure

1. Entre em https://portal.azure.com com a conta que tem a assinatura (Azure for Students, por exemplo).
2. Clique no ícone **Cloud Shell** (`>_`) no topo e escolha **Bash**. Na primeira vez, aceite criar o armazenamento.
3. Clone o repositório e rode o script:

```bash
git clone https://github.com/2TDSPO-JPG/VetPet-EntregaFinal.git
cd VetPet-EntregaFinal/scripts/azure
chmod +x *.sh
./criar-recursos.sh
```

4. O script pede a **senha do administrador do Azure SQL**. Use pelo menos 8 caracteres com maiúscula, minúscula, número e símbolo, e **guarde-a**.
5. No fim, ele mostra `DB_URL`, `DB_USERNAME` e os endereços dos Web Apps. **Copie e guarde.**

O script cria o grupo `rg-vetpet`, o servidor e o banco Azure SQL, o plano Linux B1 e os dois Web Apps Java 21.

> Se aparecer erro de região não permitida, rode `LOCAL=eastus2 ./criar-recursos.sh`.
> Se o banco gratuito não estiver disponível na assinatura, troque no script as linhas do `az sql db create` por `--service-objective Basic`.
> Os nomes precisam ser únicos na Azure. Se algum já existir, rode `SUFIXO=outronome ./criar-recursos.sh` e troque o sufixo também no `azure-pipelines.yml`.

### Liberar o seu computador no firewall do banco

Para rodar `SELECT` pelo **Editor de consultas** do portal ou pelo Azure Data Studio, o seu IP precisa estar liberado:

```bash
./liberar-meu-ip.sh SEU_IP_PUBLICO
```

O seu IP público aparece em https://meuip.com.br. No portal, a mensagem de erro do Editor de consultas também oferece **Allowlist IP** com um clique.

---

## 2. Criar o RabbitMQ (CloudAMQP)

1. Crie uma conta em https://www.cloudamqp.com.
2. **Create New Instance** → nome `vetpet`, plano **Little Lemur (Free)**, região mais próxima disponível.
3. Abra a instância e copie a **AMQP URL** (começa com `amqps://`). Ela contém usuário e senha: é um segredo.

---

## 3. Criar o projeto no Azure DevOps

### 3.1 Organização

1. Acesse https://dev.azure.com e entre com a mesma conta da Azure.
2. Se pedir, crie uma organização (por exemplo `vetpet-2tdspo`).

### 3.2 Projeto

Clique em **+ New project** e preencha exatamente:

| Campo | Valor |
|---|---|
| Project name | `Sprint 4 – Azure DevOps` |
| Description | Uma linha por integrante, no formato `RM - Nome - Turma` |
| Visibility | **Private** |
| Advanced → Version control | **Git** |
| Advanced → Work item process | **Scrum** (ou Agile) |

### 3.3 Convidar os membros

1. *Organization settings* (canto inferior esquerdo) → **Users** → **Add users**.
2. E-mail do membro, **Access level: Basic**, projeto **Sprint 4 – Azure DevOps**, grupo **Project Contributors**.
3. Repita para o Jonas e o Gabriel.

---

## 4. Conectar a Azure ao Azure DevOps

1. No projeto: **Project settings** → **Service connections** → **New service connection**.
2. Escolha **Azure Resource Manager** → **Workload Identity federation (automatic)**.
3. Selecione a **assinatura** e o grupo de recursos **rg-vetpet**.
4. **Service connection name:** `vetpet-azure` (exatamente esse nome, usado na pipeline).
5. Marque **Grant access permission to all pipelines** e salve.

---

## 5. Guardar os segredos (grupo de variáveis)

1. **Pipelines** → **Library** → **+ Variable group**.
2. **Variable group name:** `vetpet-segredos`.
3. Adicione as variáveis abaixo. Nas marcadas como secreta, clique no **cadeado** para proteger o valor.

| Nome | Valor | Secreta |
|---|---|---|
| `DB_URL` | A `DB_URL` mostrada pelo script | não |
| `DB_USERNAME` | `clyvoadmin` | **sim** |
| `DB_PASSWORD` | A senha do Azure SQL | **sim** |
| `RABBITMQ_URL` | A AMQP URL do CloudAMQP | **sim** |
| `NOTIFICACAO_API_KEY` | Uma chave longa qualquer (ex.: gerada em https://www.uuidgenerator.net) | **sim** |
| `MAIL_USERNAME` | O Gmail que envia os avisos | **sim** |
| `MAIL_PASSWORD` | A senha de app do Gmail, sem espaços | **sim** |
| `VETPET_ADMIN_SENHA` | A senha do admin do painel (`admin@vetpet.com`) | **sim** |

4. Clique em **Save**.

---

## 6. Criar a pipeline

### 6.1 Branch `master`

A pipeline dispara na `master`. No seu computador, na pasta do projeto:

```bash
git checkout -b master
git push -u origin master
```

### 6.2 Pipeline

1. **Pipelines** → **Create Pipeline** → **GitHub (YAML)**.
2. Autorize o Azure Pipelines no GitHub e escolha o repositório `2TDSPO-JPG/VetPet-EntregaFinal`.
   - Como o repositório é de uma organização do GitHub, um dono da organização `2TDSPO-JPG` pode precisar aprovar o aplicativo **Azure Pipelines**.
3. Escolha **Existing Azure Pipelines YAML file** → branch `master` → arquivo `/azure-pipelines.yml`.
4. Clique em **Run**.
5. Na primeira execução, a pipeline pode pedir permissão para usar o grupo `vetpet-segredos` e a conexão `vetpet-azure`. Clique em **View → Permit**.

### 6.3 Conferir

- **CI:** os dois jobs ficam verdes. Na aba **Tests** aparecem os testes. Em **1 published** (ou *Related → Artifacts*) aparecem os artefatos `vetpet-api` e `vetpet-notificacao`.
- **CD:** os dois deploys ficam verdes.
- Abra `https://vetpet-api-rm565824.azurewebsites.net`. A primeira subida leva 1 a 2 minutos.
- Teste a saúde: `https://vetpet-api-rm565824.azurewebsites.net/actuator/health`.

Se a aplicação não abrir: portal → Web App → **Log stream** mostra o log do Spring.

---

## 7. Conferir o CRUD no banco

No portal: **SQL databases** → `vetpet` → **Query editor** → entre com `clyvoadmin` e a senha.

As duas tabelas principais e relacionadas são `VP_TUTOR` e `VP_PET`, ligadas por `VP_TUTOR_PET`. Depois de cada operação no painel, rode a consulta correspondente:

```sql
SELECT id, nome, email, telefone FROM VP_TUTOR ORDER BY id DESC;

SELECT p.id, p.pet_code, p.nome, p.especie, p.raca, p.peso_kg, t.nome AS tutor
FROM VP_PET p
JOIN VP_TUTOR_PET tp ON tp.pet_id = p.id
JOIN VP_TUTOR t ON t.id = tp.tutor_id
ORDER BY p.id DESC;
```

| Operação | No painel | No banco |
|---|---|---|
| Inserir | Cadastrar um tutor e um pet vinculado a ele | As duas consultas mostram as linhas novas |
| Consultar | Abrir a lista e a ficha do pet | Mesmas linhas |
| Atualizar | Editar o peso ou a raça do pet | `peso_kg` / `raca` alterados |
| Excluir | Excluir o pet | A linha some de `VP_PET` e de `VP_TUTOR_PET` |

---

## Remover os recursos

```bash
./excluir-recursos.sh
```
