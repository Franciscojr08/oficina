# ADR-0002 — Flyway como fonte de verdade do schema, executado no boot

## Status

Aceito

## Contexto

O schema do banco é consumido por dois componentes de repositórios diferentes: a aplicação
(`oficina`) e a function de autenticação (`oficina-lambda`), que lê a tabela `cliente` diretamente.
A infraestrutura do banco, por sua vez, é provisionada por um terceiro repositório
(`oficina-database`).

Com o banco recriado do zero a cada sessão do Learner Lab, era preciso decidir quem reconstrói o
schema e quando.

## Alternativas consideradas

| Opção | Prós | Contras |
|---|---|---|
| **Flyway no boot da aplicação, migrations versionadas no `oficina`** | Schema nasce junto com a aplicação que o usa; versionado em código e revisado em PR; funciona igual em Docker local, CI e EKS | O primeiro Pod a subir executa as migrations — dois Pods simultâneos disputam o lock |
| `ddl-auto = update` do Hibernate | Zero configuração | Não versiona nada, não reproduz o mesmo schema duas vezes e não permite índice funcional como o da `V10` |
| Migrations no repositório `oficina-database`, junto do Terraform | Junta banco e schema no mesmo lugar | Separa o schema do código que o usa: uma mudança de entidade exigiria PR em dois repositórios, e o Terraform teria que aplicar SQL |
| Script SQL aplicado à mão | Controle total | Não reproduzível; some na próxima recriação do ambiente |

## Decisão

As migrations vivem em `src/main/resources/db/migration` **neste** repositório e rodam
automaticamente no boot (`SPRING_FLYWAY_ENABLED: "true"`). O `oficina-database` provisiona a
instância vazia; o schema é responsabilidade daqui.

Isso está registrado também no [RFC-0001 do `oficina-database`](https://github.com/rremiao/oficina-database/blob/main/docs/rfc/0001-escolha-do-banco-de-dados.md),
que documenta explicitamente que aquele repositório descreve o modelo (`docs/der.md`) mas não o
versiona — para não haver duas fontes de verdade.

## Justificativa

1. **O schema muda junto com o código que o usa.** Uma entidade nova e sua tabela entram no mesmo
   PR, revisados juntos.
2. **A `V10` é o exemplo de por que isso importa.** Ela cria um índice funcional
   (`regexp_replace(cpf_cnpj, '\D', '', 'g')`) que existe para uma consulta feita por **outro
   repositório** — a function `auth-token`. A expressão precisa ser idêntica caractere a caractere à
   do `WHERE` da function, senão o planejador ignora o índice, sem erro. Versionar essa migration
   junto do código deixa esse acoplamento visível e revisável.
3. **O Learner Lab exige reprodutibilidade.** Toda sessão recria o banco; o schema precisa se
   reconstruir sozinho até a `V10` sem intervenção manual.

## Consequências

- **Positivas**: um `kubectl apply` reconstrói o schema completo; o histórico de mudanças de banco
  fica no mesmo git do código; ambiente local (Docker Compose) e AWS usam exatamente as mesmas
  migrations.
- **Negativas / trade-offs**:
  - Migração roda no boot: com várias réplicas, o primeiro Pod aplica e os outros esperam o lock do
    Flyway. Funciona, mas atrasa o rollout, e uma migration longa seguraria o `readinessProbe`.
  - Rollback não é automático. Reverter exige uma migration nova de compensação.
  - O `oficina-lambda` depende de um schema que ele não versiona. Uma mudança em `cliente.cpf_cnpj`
    ou na expressão do índice quebra a autenticação sem que nada no repositório da function mude —
    risco registrado no contrato de autenticação (`docs/fase3/contrato-autenticacao.md`).
