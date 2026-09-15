# Regras de trabalho — Tech Challenge Fase 3 (SOAT / POS Tech)

## Git: commit e push exigem permissão explícita

Editar arquivos, criar/trocar de branch local e preparar mudanças (`git status`, `git diff`, `git add` para revisão) é sempre permitido sem perguntar.

**Nunca rode `git commit` ou `git push` sem antes pedir e receber uma confirmação explícita do usuário nesta mesma conversa** — mesmo que um pedido anterior pareça implicar isso, e mesmo que as mudanças pareçam pequenas ou óbvias.

Regra do time, além disso: **nunca commitar direto na `main`** — sempre branch + Pull Request.

Fluxo obrigatório sempre que houver mudanças prontas:

1. Rode `git status` e `git diff --stat` e mostre um resumo claro do que seria commitado.
2. Pergunte explicitamente se pode commitar e dar push, e espere a resposta.
3. Só rode `git add`, `git commit` e `git push` depois de uma confirmação afirmativa clara naquele turno.

## Nunca use `git add -A` neste repositório

O `.gitattributes` não define `* text=auto`, então cerca de 196 arquivos aparecem como modificados apenas por diferença de fim de linha (CRLF/LF), com o mesmo número de linhas adicionadas e removidas. `git add -A` ou "Stage All" arrastaria todo esse ruído para dentro do commit.

Adicione sempre arquivo a arquivo, e confira com `git diff --cached --stat` antes de commitar.

## Autenticação por CPF (Fase 3)

O cliente se autentica por CPF em duas funções serverless do repositório `oficina-lambda`:
`auth-token` emite o JWT (`POST /auth/token` no API Gateway) e `authorizer` valida o token na borda.
Esta aplicação **revalida o mesmo token de forma independente** — o LoadBalancer do EKS é público,
então o Gateway sozinho não é fronteira de segurança.

- Mecanismo no filtro: `docs/arquitetura/adr/0004-dois-tipos-de-token-jwt.md`
- Contrato (claims, status codes, segredo compartilhado): `docs/fase3/contrato-autenticacao.md`
- Índice de toda a documentação arquitetural: `docs/arquitetura/README.md`

Dois pontos que parecem detalhe e não são:

- A consulta da Lambda usa `regexp_replace(cpf_cnpj, '\D', '', 'g')`, e a migration `V10` cria um
  índice sobre **exatamente essa expressão**. Mudar uma sem a outra faz a consulta virar seq scan,
  sem erro nenhum.
- O `CorrelationIdFilter` herda `x-correlation-id` do API Gateway. Se ele deixar de rodar antes dos
  demais filtros, os logs param de correlacionar com os do Gateway e da Lambda.
