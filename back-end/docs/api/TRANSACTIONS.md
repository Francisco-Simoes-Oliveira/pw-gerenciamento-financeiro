# Transactions API

Engloba Categorias e Transações.

---

## 1. Criar Categoria
**Método HTTP**: `POST`
**URL**: `http://localhost:8080/api/categories`

### Body (JSON)
```json
{
  "name": "Alimentação",
  "type": "EXPENSE",
  "color": "#FF5733",
  "icon": "restaurant",
  "active": true,
  "systemCategory": false
}
```

---

## 2. Criar Transação (Receita / Despesa / Transferência)
**Método HTTP**: `POST`
**URL**: `http://localhost:8080/api/transactions?currentUserId={userId}`
**Query Parameters**:
- `currentUserId`: UUID do usuário criador

### Body (JSON)
```json
{
  "title": "Salário de Março",
  "amount": 5000.00,
  "type": "INCOME",
  "status": "PAID",
  "walletId": "<ID_DA_CARTEIRA>",
  "categoryId": "<ID_DA_CATEGORIA_INCOME>",
  "createdById": "<ID_DO_USUARIO>",
  "transactionDate": "2026-03-05T10:00:00"
}
```

---

## 3. Listar Transações
**Método HTTP**: `GET`
**URL**: `http://localhost:8080/api/transactions?walletId={walletId}&currentUserId={userId}`
**Query Parameters**: `walletId`, `currentUserId`
