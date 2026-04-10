# pw-gerenciamento-financeiro              

📊 Sistema de Controle Financeiro          
📌 Sobre a Atividade

Este projeto foi desenvolvido como parte da atividade “Aplicação de Controle Financeiro Pessoal e Compartilhado” .

O objetivo é construir uma aplicação web onde o usuário pode:

Gerenciar receitas e despesas
Visualizar um resumo financeiro
Simular compartilhamento de carteira com outros usuários

Nesta primeira etapa, foi desenvolvido apenas o frontend em React, utilizando dados mockados para simular integração com API.

🎯 Funcionalidades Implementadas
🔐 Autenticação
Login de usuário
Cadastro de novo usuário
Recuperação de senha (2 etapas)
Alteração de senha (área autenticada)
📊 Dashboard
Resumo financeiro (saldo, receitas, despesas)
Gráfico com dados mockados
Lista de lançamentos recentes
Logout
⚙️ Tecnologias Utilizadas
React (Vite)
JavaScript / (ou TypeScript, se estiver usando)
TailwindCSS / Shadcn UI (se estiver usando)
Recharts (para gráficos)
LocalStorage (persistência de sessão)
🧠 Decisões Técnicas
Uso de mock de dados para simular backend, permitindo desenvolvimento desacoplado
Separação por features, facilitando escalabilidade
Uso de hooks personalizados para lógica reutilizável
Persistência de autenticação via localStorage
📂 Estrutura do Projeto

```
src/
│
├── assets/ # imagens, ícones e arquivos estáticos
│
├── components/ # componentes globais reutilizáveis
├── lib/ # configurações e utils do shadcn
├── services/ # serviços globais (API, autenticação)
├── hooks/ # hooks reutilizáveis
├── utils/ # funções auxiliares
│
├── features/ # organização por domínio (feature-based)
│
│ ├── auth/ # autenticação
│ │ ├── pages/ # telas (login, cadastro, recuperação)
│ │ ├── components/ # componentes específicos
│ │ ├── services/ # lógica de autenticação
│ │ └── hooks/ # hooks de auth
│ │
│ ├── dashboard/ # área principal do sistema
│ │ ├── pages/
│ │ ├── components/
│ │ └── services/
│ │
│ ├── settings/ # configurações do usuário
│ │ ├── pages/
│ │ ├── components/
│ │ └── services/
│
├── routes/ # definição de rotas e proteção
│
├── App.jsx # componente principal
├── main.jsx # ponto de entrada

```

🔐 Fluxo de Autenticação
Login valida credenciais mockadas
Token é salvo no localStorage
Rotas protegidas verificam autenticação
Usuário não autenticado é redirecionado para login
🔄 Funcionalidades Detalhadas
🟢 Login
Validação de e-mail e senha
Feedback de erro e loading
Redirecionamento automático
🟡 Cadastro
Validação de campos
Verificação de e-mail duplicado
Indicador de força da senha
🔵 Recuperação de Senha
Etapa 1: solicitação de e-mail
Etapa 2: redefinição com token na URL
🔴 Alteração de Senha
Requer senha atual
Validação da nova senha
Rota protegida
📊 Dashboard
Dados mockados com delay (simulação de API)
Indicadores financeiros
Gráfico com Recharts
Lista de transações
🧪 Validações
Campos obrigatórios
E-mail válido
Senha mínima (6+ caracteres)
Confirmação de senha
Feedback visual (erro/sucesso/loading)
🚀 Como Executar o Projeto

```
# instalar dependências

npm install

# rodar o projeto

npm run dev

```

📈 Critérios Atendidos
✔️ Funcionalidades completas e integradas
✔️ Validação de formulários
✔️ Código organizado e modular
✔️ Boa experiência do usuário
✔️ Estrutura escalável
👨‍💻 Observações

O projeto foi estruturado pensando em:

Escalabilidade futura (backend em Spring Boot)
Separação de responsabilidades
Facilidade de manutenção
🔥 Se quiser melhorar ainda mais (dica de professor 👇)

Você pode ganhar MUITO ponto adicionando:

Prints das telas
GIF de funcionamento
Explicação do porquê escolheu essa arquitetura
