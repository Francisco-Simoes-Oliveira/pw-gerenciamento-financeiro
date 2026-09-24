# ADR 003: Uso de JPA/Hibernate

- +## Status
- +Aceito
- +## Contexto
- +O backend precisa persistir dados relacionais com padronização e suporte a evoluções futuras.
- +## Decisão
- +Usar Spring Data JPA com Hibernate como implementação de persistência.
- +## Consequências
- +- Menos código boilerplate de acesso a dados
  +- Repositórios declarativos
  +- Boa integração com transações e testes de persistência
