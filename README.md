# Sistema de Controle Financeiro

Backend Java/Spring Boot para controle financeiro pessoal e compartilhado, preparado para autenticação JWT, RBAC, documentação OpenAPI e testes automatizados.

## Tecnologias

- Java 21
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- Hibernate
- MariaDB/MySQL
- Maven
- Lombok
- JUnit 5
- Mockito
- H2 para testes
- Swagger/OpenAPI

## Arquitetura

O backend segue uma organização em camadas e por feature:

- `controller` recebe a requisição HTTP
- `service` concentra as regras de negócio
- `repository` faz a persistência com Spring Data JPA
- `dto` evita expor entidades diretamente
- `entity` representa o modelo de domínio persistido
- `mapper` converte entidade para resposta
- `security` centraliza autenticação e autorização JWT
- `common` reúne respostas padronizadas e tratamento global de exceções

### Estrutura principal

- `src/main/java/com/financeiro/backend/config`
- `src/main/java/com/financeiro/backend/security`
- `src/main/java/com/financeiro/backend/common`
- `src/main/java/com/financeiro/backend/features`

### Features iniciais

- `auth`
- `user`

As features de `dashboard`, `transaction`, `category` e `sharedwallet` já estão reservadas para expansão.

## Execução

### Backend

```bash
cd back-end
./mvnw.cmd spring-boot:run
```

### Testes

```bash
cd back-end
./mvnw.cmd test
```

## Banco de dados

Configuração local padrão:

- URL: `jdbc:mariadb://localhost:3306/financeiro_db`
- Usuário: `root`
- Senha: vazia

Os testes usam H2 em memória para não depender de infraestrutura externa.

## Segurança

- Autenticação JWT stateless
- Senhas com BCrypt
- RBAC com roles `USER` e `ADMIN`
- Filtro JWT para proteger endpoints

Endpoints públicos atuais:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /swagger-ui.html`
- `GET /api-docs`

Endpoints protegidos atuais:

- `GET /api/v1/users/{id}`
- `GET /api/v1/users/by-email?email=...`

## Documentação

- Swagger/OpenAPI habilitado via Springdoc
- ADRs em `docs/adr`

## Próximos passos naturais

- Cadastro de carteiras e membros
- Receita e despesa com categorias
- Recuperação de senha com token expirável
- Auditoria de lançamentos e dashboard financeiro
