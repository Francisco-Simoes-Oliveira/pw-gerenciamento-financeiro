# Planejamento e Modelagem de Banco de Dados

Acho que agora é a hora certa de parar de criar código e fazer a modelagem. Em projetos reais, principalmente backend, o banco costuma vir antes de boa parte das classes. Se a modelagem estiver bem feita, grande parte das entidades JPA praticamente "caem de graça".

Como já conheço o contexto do seu projeto (**controle financeiro pessoal e compartilhado, React + Spring Boot, JWT, planos, carteiras compartilhadas e sem gamificação por enquanto**), eu faria uma modelagem pensando em crescimento, mas sem exagerar.

---

## Domínio Principal

Eu dividiria em 7 módulos:

1. **Auth**
2. **Usuários**
3. **Planos**
4. **Carteiras**
5. **Categorias**
6. **Transações**
7. **Convites/Compartilhamento**

---

## Modelagem das Entidades

### User

Responsável apenas pela conta.

| Campo           | Tipo / Descrição      |
| :-------------- | :-------------------- |
| `id`            | UUID (Chave Primária) |
| `email`         | String (Único)        |
| `password`      | String                |
| `role`          | UserRole (Enum)       |
| `enabled`       | Boolean               |
| `emailVerified` | Boolean               |
| `createdAt`     | Timestamp             |
| `updatedAt`     | Timestamp             |

#### Relacionamentos

- `1 User` $\rightarrow$ `N WalletMember`
- `1 User` $\rightarrow$ `1 Subscription`
- `1 User` $\rightarrow$ `N PasswordResetToken`

---

### SubscriptionPlan

Os planos disponíveis (Exemplos: `FREE`, `PRO`, `FAMILY`).

| Campo                 | Tipo / Descrição      |
| :-------------------- | :-------------------- |
| `id`                  | UUID (Chave Primária) |
| `name`                | String                |
| `description`         | String                |
| `maxWallets`          | Integer               |
| `maxMembersPerWallet` | Integer               |
| `maxCategories`       | Integer               |
| `price`               | BigDecimal            |
| `active`              | Boolean               |

---

### UserSubscription

Plano contratado pelo usuário. Assim você consegue trocar de plano sem perder o histórico.

| Campo       | Tipo / Descrição          |
| :---------- | :------------------------ |
| `id`        | UUID (Chave Primária)     |
| `user`      | User (FK)                 |
| `plan`      | SubscriptionPlan (FK)     |
| `startDate` | Date / Timestamp          |
| `endDate`   | Date / Timestamp          |
| `status`    | SubscriptionStatus (Enum) |
| `autoRenew` | Boolean                   |

---

### Wallet

Carteiras financeiras (Exemplos: `Carteira Pessoal`, `Casa`, `Empresa`, `Viagem`).

| Campo         | Tipo / Descrição      |
| :------------ | :-------------------- |
| `id`          | UUID (Chave Primária) |
| `name`        | String                |
| `description` | String                |
| `currency`    | String                |
| `active`      | Boolean               |
| `createdAt`   | Timestamp             |
| `owner`       | User (FK)             |

---

### WalletMember

Quem participa da carteira.

| Campo        | Tipo / Descrição        |
| :----------- | :---------------------- |
| `id`         | UUID (Chave Primária)   |
| `wallet`     | Wallet (FK)             |
| `user`       | User (FK)               |
| `permission` | WalletPermission (Enum) |
| `joinedAt`   | Timestamp               |

---

### Category

Categorias de lançamentos. Assim, cada carteira pode ter suas próprias categorias.

| Campo    | Tipo / Descrição      |
| :------- | :-------------------- |
| `id`     | UUID (Chave Primária) |
| `wallet` | Wallet (FK)           |
| `name`   | String                |
| `color`  | String                |
| `icon`   | String                |
| `type`   | CategoryType (Enum)   |
| `active` | Boolean               |

---

### Transaction

A entidade mais importante do sistema.

| Campo         | Tipo / Descrição         |
| :------------ | :----------------------- |
| `id`          | UUID (Chave Primária)    |
| `wallet`      | Wallet (FK)              |
| `category`    | Category (FK)            |
| `createdBy`   | User (FK)                |
| `title`       | String                   |
| `description` | String                   |
| `amount`      | BigDecimal               |
| `type`        | TransactionType (Enum)   |
| `date`        | Date                     |
| `status`      | TransactionStatus (Enum) |
| `createdAt`   | Timestamp                |
| `updatedAt`   | Timestamp                |

---

### PasswordResetToken

Tokens para recuperação de senha.

| Campo        | Tipo / Descrição      |
| :----------- | :-------------------- |
| `id`         | UUID (Chave Primária) |
| `user`       | User (FK)             |
| `token`      | String                |
| `expiration` | Timestamp             |
| `used`       | Boolean               |

---

## Diagrama de Relacionamentos

````
SubscriptionPlan
│
│
UserSubscription
│
│
User──────────────┐      │
│                 │      │
│                 │      │
│                 ▼      │
│           WalletMember │
│                 │      │
│                 ▼      │
│              Wallet ◄──┘
│                 │
│      ┌──────────┴──────────┐
│      │                     │
▼      ▼                     ▼
Transaction             Category

User
│
▼
PasswordResetToken

---

## Enums Utilizados

```protobuf
enum UserRole {
  ADMIN
  USER
}

enum TransactionType {
  INCOME
  EXPENSE
  TRANSFER
}

enum TransactionStatus {
  PENDING
  PAID
  CANCELED
}

enum WalletPermission {
  OWNER
  EDITOR
  VIEWER
}

enum SubscriptionStatus {
  ACTIVE
  EXPIRED
  CANCELED
  TRIAL
}
````
