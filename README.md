# PW Gerenciamento Financeiro

O **PW Gerenciamento Financeiro** é um sistema de controle financeiro pessoal e compartilhado voltado para uma gestão eficiente de despesas, receitas e transferências. Ele permite organizar saldos em múltiplas carteiras (contas), controlar acessos entre diferentes membros, estruturar metas e fornecer uma visão unificada da saúde financeira do usuário através de dashboards e relatórios analíticos em tempo real.

O backend deste projeto foi projetado para oferecer robustez, integridade de dados contábeis e escalabilidade, seguindo padrões modernos de arquitetura corporativa.

---

## Tecnologias

A aplicação é construída utilizando as seguintes tecnologias e frameworks:

- **Java 21**
- **Spring Boot 4.0.6**
- **Spring Data JPA & Hibernate**
- **Spring Security & OAuth2 Resource Server**
- **Spring WebSocket** (atualização de transações em tempo real)
- **MariaDB** (Produção/Desenvolvimento)
- **H2 Database** (Ambiente de Testes)
- **Maven**
- **JWT (JJWT 0.12.6)**
- **Lombok**
- **MapStruct 1.5.5**
- **Swagger / OpenAPI (Springdoc 2.8.9)**
- **JUnit 5 & Mockito**

---

## Arquitetura

O projeto adota uma arquitetura em camadas focada em domínios (_Feature-Based_). Toda lógica e persistência estão bem separadas, respeitando o princípio de responsabilidade única (SOLID). Utilizamos o padrão de DTOs para tráfego externo e MapStruct para tradução ágil entre DTO e Entidade. As respostas HTTP são padronizadas com a classe `ApiResponse` e o tratamento de erros é centralizado no `GlobalExceptionHandler`.

### Árvore Resumida do Projeto

```text
src/main/java
└── com/financeiro/backend
    ├── common
    │   ├── dto         (ApiResponse)
    │   └── exception   (GlobalExceptionHandler)
    ├── config          (SwaggerConfig, etc.)
    ├── security        (SecurityFilterChain, Jwt Filters, PasswordReset)
    └── features
        ├── auth        (User e UserRole)
        ├── profile     (UserProfile)
        ├── wallet      (Wallet e Members)
        ├── category    (Category Income/Expense/Transfer)
        ├── transaction (Transaction core)
        ├── subscription(Plans e UserSubscription)
        ├── reports     (Queries, Projections, Dashboards)
        ├── realtime    (WebSocket, tickets efêmeros e eventos pós-commit)
        └── finance     (Engine de processamento de saldos)
```

### Estrutura de Features

- **Auth & Security**: Gerenciamento do fluxo de autenticação e solicitação de troca de senha.
- **User**: Criação, atualização e listagem da conta raiz dos usuários (Register).
- **Profile**: Dados adicionais ao usuário como avatar, nome completo e telefone.
- **Wallet**: Criação e compartilhamento de contas correntes, cartões e limites. Inclui gestão de permissões para membros (`VIEWER`, `EDITOR`, `OWNER`).
- **Category**: Classificação de finanças (Receitas, Despesas, Transferências).
- **Transaction**: Registro de todas as movimentações.
- **Subscription**: Controle de planos (ex: Premium) e gestão de cotas máximas para carteiras e categorias.
- **Reports**: Módulo exclusivamente de leitura otimizado (Dashboards, Extratos com paginação, Fluxo de Caixa). Separado da camada de transação.
- **Finance**: Core contábil interno (não exposto em Controller). Garante a consistência dos saldos das carteiras recalculando e aplicando estornos caso uma transação seja editada ou excluída.
- **Realtime**: Emite eventos somente após o commit das transações e atualiza clientes conectados por WebSocket. O handshake usa um ticket efêmero, de uso único, emitido por uma rota protegida por JWT; o token JWT não é enviado na URL do WebSocket.

---

## Banco de Dados

Abaixo, a representação estrutural unificada do banco em um diagrama relacional simplificado.

```mermaid
erDiagram
    User ||--|| UserProfile : has
    User ||--o{ Wallet : owns
    Wallet ||--o{ WalletMember : allows
    Wallet ||--o{ Category : contains
    Wallet ||--o{ Transaction : records
    Category ||--o{ Transaction : classifies
    SubscriptionPlan ||--o{ UserSubscription : offers
    User ||--o{ UserSubscription : subscribes
```

---

## Funcionalidades Implementadas

✅ Cadastro de Usuário (Register)  
✅ Perfil de Usuário  
✅ Reset de Senha (Request/Confirm)  
✅ Gestão de Assinaturas e Planos (Limites de Sistema)  
✅ Carteiras Pessoais e Compartilhadas (Membros)  
✅ Categorias (Receitas, Despesas e Transferências)  
✅ Transações de Receita e Despesa  
✅ Transferências entre Carteiras  
✅ Auditoria Financeira Interna (Engine de Saldos)  
✅ Dashboard Consolidado  
✅ Extratos com Filtros (Specifications)  
✅ Indicadores Analíticos  
✅ Atualização de transações e dashboard em tempo real via WebSocket  
✅ Documentação Swagger (OpenAPI)  
✅ Testes Unitários e Cobertura (JUnit + Mockito)

---

## Funcionalidades Futuras

O projeto continua em evolução. As seguintes funcionalidades estão no planejamento e ainda não foram implementadas:

- Gamificação e Pontuações
- Metas financeiras e Orçamentos Fixos
- Notificações de Pagamentos e Vencimentos
- Upload de comprovantes (Storage)
- Integração Open Finance / API Bancária
- Aplicativo Mobile

---

## Como Executar

### 1. Clonar o Repositório

```bash
git clone <url-do-repositorio>
cd pw-gerenciamento-financeiro/back-end
```

### 2. Configurar o Banco de Dados

A aplicação utiliza o **MariaDB** por padrão. Crie um banco local:

```sql
CREATE DATABASE financeiro_db;
```

Ajuste as credenciais no arquivo `src/main/resources/application.properties` se necessário:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/financeiro_db
spring.datasource.username=root
spring.datasource.password=sua-senha
spring.jpa.hibernate.ddl-auto=update
```

### 3. Executar o Projeto

Via Maven Wrapper (Linux/Mac):

```bash
./mvnw clean install
./mvnw spring-boot:run
```

Via Maven Wrapper (Windows):

```cmd
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

A API estará disponível localmente em: `http://localhost:8080/`

---

## Testes

A arquitetura financeira e de relatórios foi extensamente testada para garantir consistência de saldos (Transactions vs FinanceService).

Para rodar a suíte de testes unitários localmente (utilizando banco H2 em memória):

```bash
./mvnw test
```

---

## Documentação da API

1. **Swagger / OpenAPI**
   Assim que o servidor rodar, acesse a interface visual em:  
   👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

2. **Guia Completo e Testes Manuais**
   Exemplos reais e validados contra o código:
   - [docs/api/API_TESTS.md](docs/api/API_TESTS.md)

3. **Coleção Postman / Insomnia**
   Há uma coleção completa com 20 requisições prontas e padronizadas para testes diretos. Importe no seu cliente de API favorito:
   - [docs/insomnia/Financeiro_API_Insomnia.json](docs/insomnia/Financeiro_API_Insomnia.json)

---

## Segurança, Autenticação e Tempo Real

As rotas da aplicação usam autenticação JWT e o acesso aos recursos de carteira é validado no backend conforme o papel do usuário (`OWNER`, `EDITOR` ou `VIEWER`). Para o canal em tempo real, o JWT continua restrito às requisições HTTP autenticadas: o frontend solicita `POST /api/realtime/ticket`, recebe um ticket aleatório com validade de 30 segundos e usa esse ticket uma única vez no handshake de `/ws/realtime`.

Quando uma transação é criada, editada ou excluída, o backend publica um evento interno. O envio WebSocket ocorre com `AFTER_COMMIT`, evitando notificar os clientes sobre uma operação que posteriormente seja revertida. O servidor resolve os usuários autorizados das carteiras afetadas e envia o evento somente a eles.

```mermaid
flowchart TD
    A[Login] --> B[JWT]
    B --> C[POST /api/realtime/ticket]
    C --> D[Ticket efêmero de uso único]
    D --> E[WebSocket /ws/realtime]
    B --> F[Criação/Edição/Exclusão de Transaction]
    F --> G[FinancialService]
    G --> H[Commit no banco]
    H --> I[TransactionChangedEvent]
    I --> J[Usuários autorizados da carteira]
    J --> E
    E --> K[React atualiza Transações e Dashboard]
```

No frontend, `VITE_WS_BASE_URL` é opcional. Quando não configurada, a URL WebSocket é derivada automaticamente de `VITE_API_BASE_URL` (`http` → `ws` e `https` → `wss`).

---

## Roadmap

| Sprint       | Status | Descrição                                                                |
| ------------ | :----: | ------------------------------------------------------------------------ |
| **Sprint 1** |   ✅   | Criação de Estrutura, Arquitetura, Padrão DTO e Exceções                 |
| **Sprint 2** |   ✅   | Domínio Base: Usuários, Perfis, Configurações de Security                |
| **Sprint 3** |   ✅   | Core Financeiro: Transações, Carteiras, Membros, Estornos e Recálculos   |
| **Sprint 4** |   ✅   | CQRS Básico (Leitura): Dashboard, Extratos, Categorização, Indicadores   |
| **Sprint 5** |   ✅   | Segurança: Login JWT, proteção de endpoints e autorização por carteira   |
| **Sprint 6** |   🟡   | Tempo real via WebSocket, integração e testes                            |
| **Sprint 7** |   ⏳   | Notificações, Gamificação e Metas                                        |

---

## Boas Práticas Adotadas

- **DTO Pattern**: Nunca retornar ou expor Entidades (`@Entity`) diretamente nos Controllers.
- **MapStruct**: Mapeamento seguro de ponta a ponta sem boilerplate ou _Transient Exceptions_.
- **ApiResponse**: Todo output da API tem o mesmo formato unificado (`success`, `message`, `data`).
- **GlobalExceptionHandler**: Tratamento global de falhas capturando `IllegalArgumentException`, `ResourceNotFoundException` e padronizando os erros do `jakarta.validation`.
- **Clean Code & SOLID**: As lógicas financeiras mais pesadas estão segregadas no `FinancialService`, enquanto as leituras foram movidas para `FinancialReportService`, removendo as regras do banco dos Controllers.

---

## Relatório final: extrato em PDF e CSV

Abra **Relatórios** no menu (`/reports`), selecione uma carteira e as datas e clique em **Consultar**.
O mês atual é preenchido automaticamente. OWNER, EDITOR e VIEWER podem consultar e exportar
lançamentos de todos os autores nas carteiras às quais têm acesso.

- **PDF e CSV** exportam todos os lançamentos do filtro; a paginação da tela é apenas visual.
- Totais: receitas pagas, despesas pagas, transferências recebidas/enviadas e variação realizada
  (`receitas - despesas + transferências recebidas - enviadas`). Pendentes/cancelados são exibidos, sem impacto nos totais.
- Período inclusivo pela data da transação; dados antigos sem essa data usam a data de criação.
- A variação não é saldo disponível: não inclui saldo de abertura e o saldo legado da carteira aplica lançamentos sem distinguir status.
- Transferências recebidas não expõem nomes/dados privados da carteira de origem.
- A tela se atualiza pelos eventos WebSocket de transação da carteira selecionada. Cada download consulta novamente os dados e as permissões.
- Até 366 dias e 10.000 lançamentos por consulta; acima disso, reduza o período. Não há truncamento silencioso.

Endpoints autenticados por JWT:

```text
GET /api/reports/statement/report?walletId=<UUID>&startDate=2026-09-01&endDate=2026-09-30
GET /api/reports/statement/export?walletId=<UUID>&startDate=2026-09-01&endDate=2026-09-30&format=pdf
GET /api/reports/statement/export?walletId=<UUID>&startDate=2026-09-01&endDate=2026-09-30&format=csv
```

Datas omitidas em conjunto significam mês atual; informe ambas para outro período.
JSON usa `ApiResponse`; downloads retornam bytes com `Content-Disposition: attachment` e `Cache-Control: no-store`.
CSV usa UTF-8 com BOM, separador `;`, vírgula decimal e proteção contra fórmulas.
PDF usa Apache PDFBox 3.0.8 (baixado automaticamente pelo Maven), com suporte a português e paginação.
Caracteres não disponíveis na fonte PDF padrão são substituídos por `?`; o CSV mantém Unicode.

Validação local (Java 21 e Node instalados):

```sh
cd back-end
./mvnw test
cd ../Front-end
npm ci
npm run build
```

Os testes originais são mantidos. Os novos testes cobrem consultas reais em H2, autorização e revogação,
datas inclusivas, transferências, totais/status, limites, respostas HTTP, CSV e PDF com múltiplas páginas.
Para gerar um PDF sintético e imagens de conferência durante os testes:
`./mvnw test -Dstatement.qa.dir=target/statement-qa`.

Roteiro manual: compartilhe uma carteira com um VIEWER, crie receita/despesa paga, pendência e transferência;
confira os totais em ambas as contas, exporte os dois formatos e tente consultar uma carteira não compartilhada (403).
Edite um lançamento com OWNER/EDITOR em outra sessão e confirme a atualização sem F5.

A [auditoria do módulo](back-end/docs/reports-audit.md) documenta o que existia e os limites dos endpoints antigos.
Histórico de saldo e fluxo de caixa ainda são placeholders; o novo extrato não depende deles.

## Como testar o tempo real

1. Inicie backend e frontend normalmente.
2. Crie dois usuários e compartilhe uma carteira entre eles.
3. Abra o sistema em duas sessões separadas do navegador (por exemplo, janela normal e anônima) e faça login com um usuário em cada sessão.
4. Nos dois usuários, abra o Dashboard ou a tela de Transações da carteira compartilhada.
5. Em uma das sessões com permissão `OWNER` ou `EDITOR`, crie, edite ou exclua uma transação.
6. A outra sessão deve atualizar os dados automaticamente, sem recarregar a página. Na tela de Transações, alterações feitas por outro usuário também exibem uma notificação informativa.

O fluxo usado é:

```text
JWT autenticado -> POST /api/realtime/ticket -> ticket único (30s)
-> WebSocket /ws/realtime?ticket=...
-> transação confirmada no banco
-> TransactionChangedEvent AFTER_COMMIT
-> evento enviado apenas ao OWNER e membros das carteiras afetadas
-> React atualiza Dashboard/Transações
```
