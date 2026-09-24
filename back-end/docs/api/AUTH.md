# Auth / Security API

Este módulo agora suporta autenticação real via JWT (Spring Security).
O cadastro de novos usuários está localizado no domínio **Users** (`POST /api/users`).

---

## 1. Login (Autenticação JWT)
**Objetivo**: Autentica o usuário e retorna o token JWT para ser utilizado nas requisições.
**Método HTTP**: `POST`
**URL**: `http://localhost:8080/api/auth/login`
**Headers**: `Content-Type: application/json`

### Body (JSON)
```json
{
  "email": "teste@email.com",
  "password": "SenhaSegura123!"
}
```

### Exemplo de Sucesso (200 OK)
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsIn...",
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "teste@email.com",
    "name": "Nome do Usuário"
  }
}
```

---

## 1. Solicitar Redefinição de Senha
**Objetivo**: Gera e envia (mock) um token de redefinição de senha para o e-mail.
**Método HTTP**: `POST`
**URL**: `http://localhost:8080/api/auth/password-reset/request`
**Headers**: `Content-Type: application/json`

### Body (JSON)
```json
{
  "email": "teste@email.com"
}
```

### Exemplo de Sucesso (200 OK)
```json
{
  "success": true,
  "message": "Instruções de redefinição de senha enviadas por email.",
  "data": null
}
```

---

## 2. Confirmar Redefinição de Senha
**Objetivo**: Usa o token recebido no e-mail para atualizar a senha.
**Método HTTP**: `POST`
**URL**: `http://localhost:8080/api/auth/password-reset/confirm`
**Headers**: `Content-Type: application/json`

### Body (JSON)
```json
{
  "token": "d748f346-6b2a-431e-b83b-abcd1234efgh",
  "newPassword": "novaSenhaSegura123!"
}
```

### Exemplo de Sucesso (200 OK)
```json
{
  "success": true,
  "message": "Senha alterada com sucesso.",
  "data": null
}
```
