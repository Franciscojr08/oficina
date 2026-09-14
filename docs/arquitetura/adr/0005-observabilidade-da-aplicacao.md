# ADR-0005 — Observabilidade da aplicação: logs ECS, correlação e eventos de negócio

## Status

Aceito e implementado

## Contexto

O desafio pede logs estruturados em JSON com correlação entre requisições, e três dashboards que só
podem ser alimentados por dado de **negócio**: volume diário de ordens de serviço, tempo médio de
execução por status e erros nas integrações.

Duas complicações: a requisição atravessa quatro processos antes de chegar aqui (Gateway →
authorizer → LoadBalancer → Pod), e nenhuma métrica de infraestrutura sabe o que é "uma OS entregue".

A escolha da ferramenta (New Relic) está registrada no
[ADR-0002 do `oficina-kubernetes`](https://github.com/rremiao/oficina-kubernetes/blob/main/docs/adr/0002-escolha-da-ferramenta-de-observabilidade.md).
Este ADR trata do que vive no **código** desta aplicação.

## Alternativas consideradas

| Opção | Prós | Contras |
|---|---|---|
| **Logs ECS nativos + filtro de correlação + eventos customizados isolados numa classe** | Sem dependência nova para log (suporte nativo do Spring Boot 4); telemetria de negócio separada da regra | Os eventos acoplam a aplicação à API do agent New Relic em um ponto |
| Chamar a API do New Relic dentro dos services de negócio | Menos uma classe | Mistura telemetria com regra; trocar de ferramenta exigiria mexer no fluxo de negócio |
| Derivar os dashboards só de logs | Nenhum acoplamento com o agent | Exigiria parsear texto para reconstruir duração por etapa; frágil e caro de consultar |
| Métricas via Micrometer/Prometheus | Padrão aberto | Duplicaria o caminho de telemetria, já que o agent do New Relic já está no cluster |

## Decisão

Três peças, com responsabilidades separadas:

**1. Logs estruturados ECS** — suporte nativo do Spring Boot 4, sem dependência adicional:

```properties
logging.structured.format.console=ecs
logging.structured.ecs.service.name=${spring.application.name}
logging.structured.ecs.service.environment=${ENVIRONMENT:producao}
```

**2. `CorrelationIdFilter`** (`shared/observabilidade/`), com `@Order(HIGHEST_PRECEDENCE)`: lê o
cabeçalho `x-correlation-id` que o Gateway injeta a partir do `context` do authorizer, coloca no MDC
(virando campo de toda linha de log), ecoa na resposta e **remove no `finally`**. Gera um UUID
quando a chamada não veio pelo Gateway.

**3. `OrdemServicoObservabilidade`** (`shared/observabilidade/`): eventos customizados
`OrdemServicoStatusAlterado`, `OrdemServicoEtapaConcluida` e `OrdemServicoTransicaoInvalida`,
enviados ao New Relic. É o que alimenta os três dashboards.

Detalhamento dos atributos de cada evento em
[`docs/observabilidade/eventos-customizados.md`](../../observabilidade/eventos-customizados.md).

## Justificativa

1. **Rodar antes de todos os outros filtros é o que faz uma falha de autenticação aparecer
   correlacionada.** Se o filtro rodasse depois do Spring Security, requisições rejeitadas ficariam
   sem rastro.
2. **Remover do MDC no `finally` não é detalhe.** Sem isso o valor vaza para a próxima requisição
   atendida pela mesma thread do pool, e os logs passam a mentir.
3. **Telemetria isolada numa classe própria.** As chamadas ao New Relic não são regra de negócio;
   mantê-las fora do `OrdemServicoStatusService` deixa explícito o que alimenta os dashboards e
   permite trocar de ferramenta sem tocar no fluxo.
4. **As chamadas viram no-op sem o agent anexado**, então testes locais e ambiente sem instrumentação
   funcionam sem configuração extra.
5. **Dado pessoal fora do log.** O filtro mascara sequências de 8 ou mais dígitos na rota antes de
   registrá-la, porque `/clientes/documento/{documento}` carrega CPF no caminho.

## Consequências

- **Positivas**: uma requisição é rastreável do Gateway até aqui pelo mesmo identificador; os três
  dashboards do enunciado saem de eventos de negócio reais, não de proxy de infraestrutura; nenhuma
  dependência nova para logging.
- **Negativas / trade-offs**:
  - `OrdemServicoObservabilidade` importa `com.newrelic.api.agent`. É o único ponto de acoplamento
    com a ferramenta, e foi isolado de propósito — mas existe.
  - A variável `ENVIRONMENT` precisa estar no ConfigMap do `oficina-kubernetes`. Sem ela, todo log
    sai marcado como `producao`, inclusive em homologação, e os eventos se misturam no New Relic.
  - O `correlationId` é aceito do cliente quando enviado: serve para depuração, não como
    identificador único garantido.
  - O cabeçalho é truncado em 128 caracteres — um valor absurdo viraria lixo em toda linha de log.
