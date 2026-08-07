# Documentação da API - Gerenciamento Financeiro (Insomnia)

Esta documentação foi elaborada para facilitar os testes manuais da API do Gerenciamento Financeiro diretamente no Insomnia, utilizando variáveis de ambiente e coleções pré-configuradas.

## 1. Pré-requisitos
* Projeto backend em execução (`http://localhost:8080`).
* Banco de dados configurado e rodando.
* Insomnia instalado (versão mais recente recomendada).

## 2. Importação da Coleção

Você encontrará o arquivo `Financeiro_API_Insomnia.json` nesta mesma pasta.
Siga os passos:
1. Abra o Insomnia.
2. Clique no botão **"Create"** no canto superior direito e selecione **"Import From" > "File"**.
3. Escolha o arquivo `Financeiro_API_Insomnia.json`.
4. Uma nova coleção "PW Financeiro API" será adicionada ao seu workspace.

## 3. Variáveis de Ambiente (Environment)

A coleção já vem com um ambiente de desenvolvimento configurado (`dev`).
Ele possui as seguintes variáveis principais:
* `base_url`: `http://localhost:8080`
* `jwt`: Receberá o token automaticamente após o login ou criação do usuário.
* `wallet_id`: Pode ser preenchido para testar transações e relatórios filtrados.
* `category_id`: Pode ser preenchido para testar vinculações de categoria.

**Autenticação**:
Todos os endpoints (exceto Auth/Login e Criação inicial) herdam a configuração de autenticação Bearer da pasta pai, que utiliza `Bearer {{ jwt }}`.

## 4. Fluxo Completo Recomendado de Testes

Siga a sequência das pastas dentro da coleção para testar todo o ecossistema de forma correta:

1. **Auth & Users**: Crie uma conta de usuário ou realize Login.
2. **Wallets**: Crie uma Carteira para receber o dinheiro. Anote ou coloque o ID retornado na variável `wallet_id`.
3. **Categories**: Crie pelo menos uma Categoria do tipo `INCOME` e outra do tipo `EXPENSE`. Coloque um dos IDs na variável `category_id`.
4. **Transactions**:
   - Crie uma Receita (Income).
   - Crie uma Despesa (Expense).
   - Crie uma segunda Carteira e teste a Transferência (Transfer) entre elas.
5. **Reports**:
   - Consulte o Dashboard consolidado.
   - Puxe os Indicadores Financeiros.
   - Obtenha o Extrato detalhado com filtros.

## 5. Tratamento de Erros

A API possui o `GlobalExceptionHandler` que padroniza os erros em objetos `ApiResponse`. Você observará retornos como:
* `400 Bad Request`: Falha na validação dos campos (ex: tentar criar receita com categoria de despesa).
* `401 Unauthorized`: Token ausente ou expirado.
* `403 Forbidden`: Tentar alterar uma carteira que pertence a outro usuário.
* `404 Not Found`: Tentar editar uma transação ou entidade que não existe.

## 6. Checklist de Validação

- [ ] Login e Geração de Token JWT
- [ ] Criação de Usuário
- [ ] Criação de Carteira
- [ ] Criação de Categoria
- [ ] Criação de Receita
- [ ] Criação de Despesa
- [ ] Criação de Transferência
- [ ] Relatório: Dashboard Sumário
- [ ] Relatório: Indicadores
- [ ] Relatório: Evolução Mensal
- [ ] Relatório: Extrato com Paginação
- [ ] Todos os cenários rodaram 200 OK ou erros tratados.
