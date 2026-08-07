# Reports API

Endpoints otimizados para leitura (Projections) para preenchimento de Dashboards.

---

## 1. Dashboard Sumário
**Objetivo**: Resumo geral das finanças (Saldo, Total Rendas, Total Despesas).
**Método HTTP**: `GET`
**URL**: `http://localhost:8080/api/reports/dashboard`
**Query Parameters** (Opcionais):
- `walletId`: Filtrar por carteira específica.
**Headers**: `Authorization: Bearer <TOKEN>`

### Exemplo de Sucesso (200 OK)
```json
{
  "success": true,
  "data": {
    "currentBalance": 12450.80,
    "monthlyIncome": 5200.00,
    "monthlyExpense": 3180.55,
    "monthlyTransfer": 850.00,
    "wallets": 3,
    "transactions": 142,
    "categories": 18
  }
}
```

---

## 2. Extrato (Statement)
**Objetivo**: Listar transações de forma enriquecida com paginação.
**Método HTTP**: `GET`
**URL**: `http://localhost:8080/api/reports/statement?page=0&size=20&type=EXPENSE`
**Query Parameters**:
- `page`, `size` (Inteiros)
- `walletId`, `categoryId` (UUID)
- `startDate`, `endDate` (YYYY-MM-DD)
- `type` (INCOME, EXPENSE, TRANSFER)
- `status` (PENDING, PAID, CANCELED)

### Exemplo de Sucesso (200 OK)
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "transactionId": "abc...",
        "walletName": "Conta Corrente Nubank",
        "categoryName": "Alimentação",
        "categoryColor": "#FF0000",
        "type": "EXPENSE",
        "amount": 45.00
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

## 3. Indicadores (Indicators)
**Objetivo**: Mostrar o Ticket Médio, Maior Despesa.
**Método HTTP**: `GET`
**URL**: `http://localhost:8080/api/reports/indicators`

### Exemplo de Sucesso (200 OK)
```json
{
  "success": true,
  "data": {
    "maxIncome": 5000.00,
    "maxExpense": 1200.00,
    "transactionCount": 42
  }
}
```
