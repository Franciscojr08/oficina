# Diagrama de Sequência — Abertura de Ordem de Serviço

Fluxo interno da aplicação: não atravessa Lambda nenhuma, mas passa pelo
API Gateway como qualquer rota protegida (ver
[diagrama de sequência da autenticação](diagrama-sequencia-autenticacao.md)).

```mermaid
sequenceDiagram
    autonumber
    actor U as Atendente / Cliente autenticado
    participant GW as API Gateway
    participant API as oficina-api (Spring)
    participant SVC as OrdemServicoService
    participant DB as RDS PostgreSQL

    U->>GW: POST /oficina/v1/ordens<br/>Bearer token + x-correlation-id
    GW->>API: $default (apos o authorizer autorizar)
    API->>API: CorrelationIdFilter + JwtAuthenticationFilter
    API->>SVC: criarOrdemServico(dto)

    SVC->>DB: SELECT cliente / SELECT veiculo
    SVC->>SVC: valida cliente ativo, veiculo ativo,<br/>veiculo pertence ao cliente, servicos e itens validos

    alt regra de negocio violada
        SVC-->>U: 400 / 422 com a mensagem da validacao
    else dados validos
        SVC->>DB: INSERT ordem_servico (status RECEBIDA)
        Note over DB: trigger BEFORE INSERT gera<br/>codigo OS-AAAA-NNNNNN via os_contador
        DB-->>SVC: OS com id e codigo

        loop cada servico do payload
            SVC->>DB: INSERT servico_ordem_servico (valor congelado)
        end
        loop cada item do payload
            SVC->>DB: INSERT item_ordem_servico (quantidade + valor congelado)
            SVC->>DB: UPDATE estoque / INSERT movimentacao_estoque
        end

        SVC->>DB: INSERT historico_ordem_servico (status RECEBIDA)
        SVC-->>U: 201 Created com o codigo da OS
    end
```

## Pontos que o diagrama revela

- **O código da OS não é gerado pela aplicação.** Um trigger `BEFORE
  INSERT` calcula `OS-2026-000001` a partir da tabela `os_contador`, com
  reset anual automático — detalhe na seção 4.2 de
  [der.md do oficina-database](https://github.com/rremiao/oficina-database/blob/main/docs/der.md).
- **Valores são congelados na associativa.** `servico_ordem_servico` e
  `item_ordem_servico` guardam o `valor_unitario` do momento, não uma
  referência ao preço atual do catálogo.
- **Cada serviço da OS tem ciclo de vida próprio** (`status`,
  `data_inicio`, `data_fim` em `servico_ordem_servico`), separado do status
  da OS como um todo.
- **`historico_ordem_servico` registra cada transição**, e é a fonte do
  dashboard de tempo médio de execução por status no New Relic.
- A baixa de estoque grava em `movimentacao_estoque` com o
  `ordem_servico_id` — a coluna **sem FK** descrita na seção 4.4 do
  documento de modelo de dados.

## Autorização

Tanto um token de operador (`ROLE_ADMIN`) quanto um token de cliente
(`ROLE_CLIENTE`, emitido pela Lambda) alcançam as rotas protegidas hoje —
a separação por perfil por rota está registrada como pendência em
[RFC-0002](rfc/0002-estrategia-de-autenticacao.md).

> Existe também uma versão deste fluxo em SVG/PNG, produzida na branch
> `docs/OS` do repositório (`docs/fluxo-abertura-os.svg`). As duas
> descrevem o mesmo fluxo — convém manter só uma como referência oficial.
