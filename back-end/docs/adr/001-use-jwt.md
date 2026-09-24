# ADR 001: Uso de JWT

- +## Status
- +Aceito
- +## Contexto
- +O sistema precisa autenticar usuários de forma stateless para integrar com um frontend React separado.
- +## Decisão
- +Adotar JWT para autenticação e autorização, com senha persistida apenas como hash BCrypt.
- +## Consequências
- +- API sem sessão server-side
  +- Melhor suporte a frontend desacoplado
  +- Possibilidade de expiração e renovação de tokens em futuras versões
