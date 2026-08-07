Atualização completa do README.md

O projeto evoluiu significativamente desde a criação do README atual. Diversas features, módulos e padrões arquiteturais foram implementados, tornando a documentação desatualizada.

Objetivo

Reescrever completamente o arquivo README.md para refletir o estado atual do projeto.

Antes de escrever qualquer conteúdo, analise todo o código-fonte.

A documentação deve ser baseada no que realmente existe no projeto, e não em funcionalidades planejadas.

Regras Obrigatórias

1. Não inventar funcionalidades

Não documente nada que não exista.

Antes de mencionar uma feature, confirme sua implementação analisando:

Controllers
Services
Entities
Repositories
DTOs
Configurações
Testes

Caso alguma funcionalidade esteja incompleta, informe isso claramente.

2. Estrutura do README

O README deve possuir, no mínimo, as seguintes seções.

Nome do Projeto

Descrição objetiva do sistema.

Objetivo

Explicar que o sistema é um gerenciador financeiro pessoal e compartilhado.

Descrever os principais recursos já implementados.

Tecnologias

Listar todas as tecnologias realmente utilizadas.

Exemplo:

Java 21
Spring Boot
Spring Security
Spring Data JPA
Hibernate
MariaDB
Maven
JWT
Lombok
MapStruct
Swagger/OpenAPI
JUnit 5
Mockito
Arquitetura

Explicar a arquitetura utilizada.

Exemplo:

Controller
Service
Service Interface
Repository
Entity
DTO
Mapper (MapStruct)
Security
Common
Exception Handler

Mostrar uma árvore resumida do projeto.

Exemplo:

src/main/java

common

config

security

features

auth

user

profile

wallet

category

transaction

subscription

reports

finance
Estrutura de Features

Explicar a responsabilidade de cada módulo.

Por exemplo:

Auth
Login
JWT
Reset de senha
User
Cadastro
Atualização
Exclusão
Profile
Informações complementares do usuário
Wallet
Carteiras
Category
Categorias
Transaction
Receitas
Despesas
Transferências
Subscription
Planos
Reports
Dashboard
Indicadores
Extratos
Finance
Processamento financeiro
Recalculo de saldo
Auditoria
Banco de Dados

Explicar as principais entidades.

Incluir um diagrama simples em Mermaid.

Exemplo:

erDiagram

User ||--|| UserProfile

User ||--o{ Wallet

Wallet ||--o{ Category

Wallet ||--o{ Transaction

Category ||--o{ Transaction

SubscriptionPlan ||--o{ UserSubscription
Funcionalidades Implementadas

Criar uma checklist.

Exemplo:

✅ Login

✅ JWT

✅ Cadastro de usuário

✅ Perfil

✅ Carteiras

✅ Categorias

✅ Receitas

✅ Despesas

✅ Transferências

✅ Auditoria Financeira

✅ Dashboard

✅ Relatórios

✅ Swagger

✅ Testes Unitários
Funcionalidades Futuras

Listar apenas funcionalidades ainda não implementadas.

Exemplo:

Gamificação
Metas financeiras
Notificações
Upload de comprovantes
Integração bancária
Aplicativo mobile
Como executar

Documentar todo o processo.

Clonar
git clone ...
Banco

Criar banco

CREATE DATABASE financeiro_db;
Configurar

Mostrar exemplo do application.properties.

Rodar
./mvnw spring-boot:run

ou

mvn spring-boot:run
Swagger

Informar a URL correta.

Exemplo:

http://localhost:8080/swagger-ui.html

ou a URL realmente implementada no projeto.

Documentação da API

Informar onde está:

docs/api/API_TESTS.md

e

docs/insomnia/
Testes

Explicar como executar.

mvn test
mvn clean install
Segurança

Explicar resumidamente:

JWT
PasswordEncoder
Password Reset
Spring Security
Fluxo Geral

Criar um fluxograma Mermaid.

Exemplo:

flowchart TD

Login

↓

JWT

↓

Wallet

↓

Category

↓

Transaction

↓

FinancialService

↓

Reports

↓

Dashboard
Roadmap

Criar uma tabela.

Sprint Status Descrição
Sprint 1 ✅ Arquitetura
Sprint 2 ✅ Regras de negócio
Sprint 3 ✅ Core Financeiro
Sprint 4 ✅ Dashboards e Relatórios
Sprint 5 ⏳ Gamificação
Boas Práticas

Documentar os padrões adotados.

DTOs
ApiResponse
GlobalExceptionHandler
MapStruct
Interfaces de Service
Testes Unitários
Princípios SOLID
Clean Code
Qualidade

O README deve servir como documentação oficial do projeto.

Ele deve permitir que um desenvolvedor novo compreenda:

o objetivo do sistema;
a arquitetura utilizada;
como executar o projeto;
como testar a API;
como está organizada a base de código;
quais funcionalidades já existem;
quais ainda serão implementadas.

Não utilizar informações fictícias. Todo o conteúdo deve ser validado contra o código-fonte atual antes de ser escrito.
