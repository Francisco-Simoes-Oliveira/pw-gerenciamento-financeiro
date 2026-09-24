# Wallets API

Endpoints responsáveis pela gestão das contas bancárias/carteiras.

---

## 1. Criar Carteira
**Método HTTP**: `POST`
**URL**: `http://localhost:8080/api/wallets?ownerId={userId}`
**Query Parameters**:
- `ownerId`: UUID do dono da carteira (atualmente exigido na URL)

### Body (JSON)
```json
{
  "name": "Conta Corrente Nubank",
  "description": "Conta principal do dia-a-dia",
  "currency": "BRL",
  "color": "#8A05BE",
  "icon": "bank"
}
```

---

## 2. Listar Carteiras do Usuário
**Método HTTP**: `GET`
**URL**: `http://localhost:8080/api/wallets?ownerId={userId}`
**Query Parameters**:
- `ownerId`: UUID do dono da carteira

---

## 3. Adicionar Membro à Carteira
**Método HTTP**: `POST`
**URL**: `http://localhost:8080/api/wallets/{id}/members/{targetUserId}?currentUserId={userId}&permission=VIEWER`
**Path Parameters**: 
- `id` (UUID da Carteira)
- `targetUserId` (UUID do Usuário convidado)
**Query Parameters**:
- `currentUserId`: UUID do dono da carteira
- `permission`: `VIEWER`, `EDITOR` ou `OWNER`
