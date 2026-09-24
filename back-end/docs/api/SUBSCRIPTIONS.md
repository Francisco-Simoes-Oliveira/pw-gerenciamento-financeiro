# Subscriptions API

Gerencia a assinatura de Planos.

---

## 1. Criar Plano (Admin)
**Método HTTP**: `POST`
**URL**: `http://localhost:8080/api/plans`

### Body (JSON)
```json
{
  "name": "Premium",
  "description": "Plano sem limites",
  "price": 19.90,
  "maxWallets": 10,
  "maxMembersPerWallet": 5,
  "maxCategories": 50,
  "active": true,
  "displayOrder": 1
}
```

---

## 2. Assinar um Plano (User)
**Método HTTP**: `POST`
**URL**: `http://localhost:8080/api/subscriptions`

### Body (JSON)
```json
{
  "userId": "<ID_DO_USUARIO>",
  "planId": "<ID_DO_PLANO>",
  "autoRenew": true
}
```
