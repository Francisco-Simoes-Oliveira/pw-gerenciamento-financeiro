# ADR 002: Arquitetura baseada em features

- +## Status
- +Aceito
- +## Contexto
- +O domínio financeiro tende a crescer com várias áreas de negócio e regras específicas por módulo.
- +## Decisão
- +Organizar o backend por feature, mantendo controller, service, repository, dto, entity e mapper próximos.
- +## Consequências
- +- Menor acoplamento entre módulos
  +- Evolução mais simples por domínio
  +- Facilita testes isolados por feature
