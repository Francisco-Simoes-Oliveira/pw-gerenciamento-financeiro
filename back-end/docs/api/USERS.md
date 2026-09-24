# Users & Profiles API

Gerenciamento da conta principal do Usuário e de seu Perfil público.

---

## 1. Registrar / Criar Usuário (Register)
**Objetivo**: Insere um novo usuário na base (Não gera token).
**Método HTTP**: `POST`
**URL**: `http://localhost:8080/api/users`
**Headers**: `Content-Type: application/json`

### Body (JSON)
```json
{
  "name": "Maria Silva",
  "email": "maria@email.com",
  "password": "senhaForte123!"
}
```

### Exemplo de Sucesso (200 OK)
```json
{
  "success": true,
  "data": {
    "id": "e42e4567-e89b-12d3-a456-426614174000",
    "name": "Maria Silva",
    "email": "maria@email.com"
  }
}
```

---

## 2. Criar Perfil de Usuário
**Objetivo**: Vincula informações extras a um usuário recém-criado.
**Método HTTP**: `POST`
**URL**: `http://localhost:8080/api/users/{userId}/profile`
**Path Parameters**: `userId` (UUID do usuário recém criado)

### Body (JSON)
```json
{
  "userId": "e42e4567-e89b-12d3-a456-426614174000",
  "fullName": "Maria José Silva",
  "birthDate": "1995-05-20",
  "phone": "+5511999999999",
  "avatarUrl": "https://avatar.com/maria.png"
}
```

---

## 3. Listar Todos Usuários
**Método HTTP**: `GET`
**URL**: `http://localhost:8080/api/users`
