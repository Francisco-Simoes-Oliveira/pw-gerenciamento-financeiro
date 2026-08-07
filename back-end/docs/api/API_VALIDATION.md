# Relatório de Validação de Endpoints

Esta validação garante que 100% da documentação reflita o código implementado. Não há endpoints fictícios.

## Resultados da Auditoria de Controllers

| Endpoint | Existe | Controller | Método |
| :--- | :---: | :--- | :--- |
| `POST /api/auth/login` | ❌ | **NÃO IMPLEMENTADO** | O Spring Security foi configurado (SecurityConfig), mas o controller ou filter de login (e geração de JWT) ainda **não foi criado**. |
| `POST /api/auth/register` | ❌ | **NÃO IMPLEMENTADO** | (O cadastro é feito via `POST /api/users` no UserController). |
| `POST /api/auth/password-reset/request` | ✅ | `PasswordResetController` | `requestReset()` |
| `POST /api/auth/password-reset/confirm` | ✅ | `PasswordResetController` | `confirmReset()` |
| `GET /api/users` | ✅ | `UserController` | `listAll()` |
| `GET /api/users/{id}` | ✅ | `UserController` | `searchById()` |
| `POST /api/users` | ✅ | `UserController` | `insert()` (Cadastro de Usuário) |
| `PUT /api/users/{id}` | ✅ | `UserController` | `alter()` |
| `DELETE /api/users/{id}` | ✅ | `UserController` | `remove()` |
| `GET /api/users/{userId}/profile` | ✅ | `UserProfileController` | `getProfile()` |
| `POST /api/users/{userId}/profile` | ✅ | `UserProfileController` | `createProfile()` |
| `PUT /api/users/{userId}/profile` | ✅ | `UserProfileController` | `updateProfile()` |
| `POST /api/wallets` | ✅ | `WalletController` | `insert()` (Requer Query `ownerId`) |
| `GET /api/wallets` | ✅ | `WalletController` | `listByOwner()` (Requer Query `ownerId`) |
| `GET /api/wallets/{id}` | ✅ | `WalletController` | `searchById()` (Requer Query `currentUserId`) |
| `PUT /api/wallets/{id}` | ✅ | `WalletController` | `alter()` (Requer Query `currentUserId`) |
| `DELETE /api/wallets/{id}` | ✅ | `WalletController` | `remove()` (Requer Query `currentUserId`) |
| `POST /api/wallets/{id}/members/{targetUserId}`| ✅ | `WalletController` | `addMember()` (Requer Query `currentUserId`, `permission`) |
| `DELETE /api/wallets/{id}/members/{targetUserId}`| ✅ | `WalletController` | `removeMember()` (Requer Query `currentUserId`) |
| `GET /api/categories` | ✅ | `CategoryController` | `listByWallet()` (Requer Query `walletId`) |
| `POST /api/categories` | ✅ | `CategoryController` | `insert()` |
| `GET /api/categories/{id}` | ✅ | `CategoryController` | `searchById()` |
| `PUT /api/categories/{id}` | ✅ | `CategoryController` | `alter()` |
| `DELETE /api/categories/{id}` | ✅ | `CategoryController` | `remove()` |
| `POST /api/transactions` | ✅ | `TransactionController` | `insert()` (Requer Query `currentUserId`) |
| `GET /api/transactions` | ✅ | `TransactionController` | `listByWallet()` (Requer Query `walletId`, `currentUserId`) |
| `GET /api/transactions/{id}` | ✅ | `TransactionController` | `searchById()` (Requer Query `currentUserId`) |
| `PUT /api/transactions/{id}` | ✅ | `TransactionController` | `alter()` (Requer Query `currentUserId`) |
| `DELETE /api/transactions/{id}` | ✅ | `TransactionController` | `remove()` (Requer Query `currentUserId`) |
| `GET /api/plans` | ✅ | `SubscriptionPlanController`| `listAll()` |
| `GET /api/plans/{id}` | ✅ | `SubscriptionPlanController`| `searchById()` |
| `POST /api/plans` | ✅ | `SubscriptionPlanController`| `insert()` |
| `PUT /api/plans/{id}` | ✅ | `SubscriptionPlanController`| `alter()` |
| `DELETE /api/plans/{id}` | ✅ | `SubscriptionPlanController`| `remove()` |
| `POST /api/subscriptions` | ✅ | `UserSubscriptionController`| `create()` |
| `GET /api/subscriptions/{id}` | ✅ | `UserSubscriptionController`| `searchById()` |
| `GET /api/subscriptions/user/{userId}`| ✅ | `UserSubscriptionController`| `searchByUserId()` |
| `PUT /api/subscriptions/{id}` | ✅ | `UserSubscriptionController`| `alter()` |
| `GET /api/reports/dashboard` | ✅ | `FinancialReportController` | `getDashboard()` (Requer RequestAttribute `userId`) |
| `GET /api/reports/monthly` | ✅ | `FinancialReportController` | `getMonthlyBalance()` |
| `GET /api/reports/categories` | ✅ | `FinancialReportController` | `getExpensesByCategory()` |
| `GET /api/reports/balance/history`| ✅ | `FinancialReportController` | `getBalanceHistory()` |
| `GET /api/reports/cashflow` | ✅ | `FinancialReportController` | `getCashFlow()` |
| `GET /api/reports/statement` | ✅ | `FinancialReportController` | `getStatement()` |
| `GET /api/reports/indicators` | ✅ | `FinancialReportController` | `getIndicators()` |

## Observações e Correções

1. **Autenticação**: O sistema não possui endpoint de `Login` (para obter o JWT) implementado ainda. A documentação do Insomnia foi ajustada para remover o Login e evidenciar que a criação de usuários ocorre em `POST /api/users`. Recomenda-se implementar o SecurityFilterChain JWT e o `AuthController` na próxima Sprint.
2. **Atributos de Sessão**: Vários endpoints estão exigindo identificadores via `@RequestParam` (`ownerId`, `currentUserId`) que futuramente devem ser extraídos diretamente do Token JWT via escopo da requisição.
3. **Validação de DTOs**: Os campos dos arquivos Insomnia agora correspondem perfeitamente aos atributos das classes Java (ex: `destinationWalletId` na transferência, `transactionDate`, etc).
