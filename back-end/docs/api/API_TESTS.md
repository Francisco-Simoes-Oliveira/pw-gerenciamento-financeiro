# Documentação de Testes da API

Esta pasta contém a documentação completa de **TODOS** os endpoints *atualmente implementados* na API do Gerenciamento Financeiro. 

Nenhum endpoint fictício foi documentado. O que você vê é o que existe no código.

## Módulos Documentados

A documentação está dividida por domínio nos seguintes arquivos:

1. [Auth](AUTH.md): Redefinição de senha (O Login ainda não está implementado).
2. [Users](USERS.md): Cadastro de novos Usuários (Register), Gerenciamento e Perfis.
3. [Wallets](WALLETS.md): Criação, listagem e compartilhamento de Carteiras.
4. [Transactions](TRANSACTIONS.md): Categorias, Receitas, Despesas e Transferências.
5. [Reports](REPORTS.md): Dashboards, Extratos, Indicadores e Cashflow.
6. [Subscriptions](SUBSCRIPTIONS.md): Planos de Assinatura e Limites do Sistema.

## Como Ler a Documentação

Cada endpoint listado nos arquivos acima obedece ao seguinte padrão:
- **Nome e Objetivo**: O que o endpoint faz.
- **Método HTTP e URL Completa**: Exemplo exato.
- **Headers e Autenticação**.
- **Parâmetros**: Detalhamento de Query Variables (ex: `currentUserId` ou `walletId`).
- **Body Completo**: JSON validado exatamente com os campos dos DTOs do Spring.

## Cadeia Completa de Execução Atual

Devido à ausência atual do endpoint `/login`, o fluxo contorna passando os IDs de usuário via Query Parameters, conforme suportado pela API hoje:

1. **Registrar Usuário** -> `POST /api/users` (Guarde o UUID do usuário gerado)
2. **Criar Perfil** -> `POST /api/users/{userId}/profile`
3. **Criar Plano e Assinar** -> `POST /api/plans` e `POST /api/subscriptions` (Vinculando o `userId` gerado)
4. **Criar Carteira** -> `POST /api/wallets?ownerId={userId}` (Guarde o ID da carteira)
5. **Criar Categoria (Receita)** -> `POST /api/categories` com type `INCOME` (Guarde o ID)
6. **Criar Categoria (Despesa)** -> `POST /api/categories` com type `EXPENSE` (Guarde o ID)
7. **Criar Receita** -> `POST /api/transactions?currentUserId={userId}`
8. **Criar Despesa** -> `POST /api/transactions?currentUserId={userId}`
9. **Criar Segunda Carteira** -> `POST /api/wallets?ownerId={userId}`
10. **Criar Transferência** -> `POST /api/transactions?currentUserId={userId}` do tipo `TRANSFER` e informando `destinationWalletId`.
11. Os relatórios (`/api/reports/...`) exigem injeção via RequestAttribute, indicando que devem ser chamados dentro de um contexto autenticado real.

> **Dica**: Utilize a coleção do Insomnia (`docs/insomnia/Financeiro_API_Insomnia.json`) para testar os endpoints funcionais com agilidade, já com as validações de Query Parameters corretas.
