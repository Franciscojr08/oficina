# RFC-0002 — Estratégia de autenticação do sistema

## Status

Aceito e implementado

## Contexto

Este RFC registra a estratégia de autenticação do **sistema como um todo**, do ponto de vista da
aplicação. O detalhe da emissão do token — contrato da rota, claims, códigos de resposta — está no
[RFC-0001 do `oficina-lambda`](https://github.com/rremiao/oficina-lambda/blob/main/docs/rfc/0001-autenticacao-por-cpf.md).

Até a Fase 2 existia um único ator autenticado: o **operador** da oficina, com e-mail e senha. A
Fase 3 exige que o **cliente final** se identifique por **CPF**, autenticado por uma function
serverless, atrás de um API Gateway.

## Alternativas avaliadas

| Opção | Prós | Contras |
|---|---|---|
| **Gateway com Lambda Authorizer + revalidação na aplicação** | Token inválido morre na borda, sem gastar recurso do cluster; a aplicação não depende da decisão do Gateway | Mais peças; verificação de assinatura acontece duas vezes |
| Gateway como proxy puro, validação só na aplicação | Menos peças e menos latência | Requisição com token inválido consumiria CPU do cluster e conexão de banco antes de ser recusada |
| Mover também a autenticação de operador para a Lambda | Um único ponto de emissão | Ampliaria o escopo da function para ler `usuario` e validar hash BCrypt, e quebraria a suíte de testes de controller que autentica via `/auth/login` |
| Amazon Cognito para os dois atores | Serviço gerenciado, com refresh e revogação | Desenhado para credencial com segredo; identificação por CPF sem senha não é o caso natural. Exigiria migrar operadores e reescrever o `SecurityConfig` para JWKS |

## Decisão

**Dois atores, dois emissores, um único formato de token.**

| Ator | Credencial | Emissor | Validação na aplicação |
|---|---|---|---|
| Operador | e-mail + senha (BCrypt) | a própria aplicação, em `POST /oficina/v1/auth/login` | recarrega o usuário do banco a cada requisição |
| Cliente | CPF | function `auth-token`, em `POST /auth/token` | assinatura e expiração apenas, sem ida ao banco |

Os dois tokens são HS256 assinados com o mesmo segredo e diferenciados pela claim `tipo` — ver
[ADR-0004](../adr/0004-dois-tipos-de-token-jwt.md) para o mecanismo no filtro.

O login de operador permaneceu na aplicação de propósito: mudá-lo não era exigido pelo enunciado e
teria custo alto em testes.

## Justificativa

1. **A borda protege o cluster; a aplicação protege a si mesma.** Como o Service do EKS é público, o
   Gateway é uma barreira, não uma fronteira de rede — por isso a revalidação existe.
2. **Escopo mínimo para a function.** Ela lê apenas `cliente`; não conhece `usuario`, hash de senha
   nem perfis.
3. **Um formato de token só.** O resto da aplicação continua lidando com um `Authentication` do
   Spring Security, sem saber de onde o token veio.

## Consequências

- **Positivas**: requisito de autenticação por CPF atendido sem provedor de identidade novo; o fluxo
  de operador seguiu intacto, com os testes existentes passando.
- **Negativas / trade-offs**:
  - Segredo HS256 simétrico compartilhado entre três componentes. Rotação exige atualizar o Secret
    do Kubernetes e a variável do Terraform da Lambda de forma coordenada; enquanto divergirem, o
    token é emitido normalmente e a aplicação responde `403`.
  - Sem revogação: um token vale até expirar, mesmo que o cliente seja desativado depois.
  - **Autorização não separa perfis.** Um token de cliente alcança hoje as mesmas rotas que um de
    operador. O enunciado pede autenticação, que está atendida; a separação por perfil está
    registrada como próximo passo em [ADR-0004](../adr/0004-dois-tipos-de-token-jwt.md).
