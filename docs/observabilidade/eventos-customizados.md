# Observabilidade da aplicação: logs estruturados e eventos de negócio

Este documento cobre a parte de observabilidade que vive no **código** da aplicação — logs e
métricas de negócio. A configuração do lado do New Relic (Infrastructure agent, APM, dashboards,
alertas, Synthetic Monitor) está documentada no repositório
[`oficina-kubernetes`](https://github.com/rremiao/oficina-kubernetes), que é quem provisiona e
configura a ferramenta de observabilidade.

## Logs estruturados (JSON)

Desde o Spring Boot 4 (usado neste projeto), logging estruturado é suporte nativo — não precisa de
nenhuma dependência nova. Habilitado em `application-docker.properties`:

```properties
logging.structured.format.console=ecs
logging.structured.ecs.service.name=${spring.application.name}
logging.structured.ecs.service.environment=${ENVIRONMENT:producao}
```

Isso faz cada linha de log sair no formato [ECS (Elastic Common Schema)](https://www.elastic.co/guide/en/ecs/current/index.html),
por exemplo:

```json
{
  "@timestamp": "2026-09-14T13:32:09.915630614Z",
  "log": {"level": "INFO", "logger": "...CorrelationIdFilter"},
  "service": {"name": "oficina", "version": "0.0.1-SNAPSHOT", "environment": "producao"},
  "message": "requisicao_atendida metodo=GET rota=/oficina/v1/api-docs status=200 duracaoMs=15",
  "correlationId": "a5608410-e57f-4d43-9bcf-ea0f26abd323",
  "ecs": {"version": "8.11"}
}
```

### Correlação entre requisições

O `CorrelationIdFilter` (`shared/observabilidade/CorrelationIdFilter.java`) roda antes de qualquer
outro filtro (`@Order(Ordered.HIGHEST_PRECEDENCE)`) e:

1. Lê o cabeçalho `x-correlation-id` — preenchido pelo API Gateway/Lambda authorizer quando a
   requisição vem de lá (ver `oficina-lambda`). Se não vier, gera um novo UUID.
2. Coloca o valor no MDC do SLF4J, sob a chave `correlationId` — é isso que faz o campo aparecer
   automaticamente em **toda** linha de log da requisição, sem precisar passar o valor manualmente
   por cada camada do código.
3. Devolve o mesmo valor no header de resposta, pra quem chamou também conseguir rastrear.
4. Ao final da requisição, loga uma linha resumo (`requisicao_atendida`) com método, rota (sem
   documentos em claro no path), status e duração.

Isso fecha o requisito de "correlação entre requisições" atravessando os componentes: o mesmo
`correlationId` que nasce no `authorizer` da Lambda (ou é gerado aqui, se a chamada não passar pelo
Gateway) aparece nos logs desta API.

## Eventos customizados de negócio

A classe `shared/observabilidade/OrdemServicoObservabilidade.java` emite eventos customizados pro
New Relic via a API `com.newrelic.agent.java:newrelic-api` (dependência adicionada no `pom.xml` —
vira no-op quando o agent Java não está anexado ao processo, então não quebra testes locais nem
outros ambientes sem instrumentação).

São 3 tipos de evento, cada um alimentando um dos 3 dashboards pedidos no desafio:

### `OrdemServicoStatusAlterado`
Emitido em toda transição de status (inclusive na criação, onde `statusAnterior = "N/A"`).

| Atributo | Descrição |
|---|---|
| `ordemServicoId` | ID da OS |
| `codigo` | Código legível (`OS-2026-000123`) |
| `statusAnterior` | Status de origem, ou `"N/A"` na criação |
| `statusNovo` | Status de destino |

**Alimenta**: dashboard de volume diário de ordens de serviço
(`WHERE statusAnterior = 'N/A'` filtra só as criações).

### `OrdemServicoEtapaConcluida`
Emitido quando uma das 3 etapas nomeadas pelo desafio (Diagnóstico, Execução, Finalização) é
concluída, com a duração calculada a partir das datas que a própria entidade `OrdemServico` já
mantém — sem precisar de nenhuma tabela ou consulta nova:

| Etapa | Calculada entre |
|---|---|
| `DIAGNOSTICO` | `dataCadastro` → `dataEnvioAprovacao` |
| `EXECUCAO` | `dataInicioExecucao` → `dataFimExecucao` |
| `FINALIZACAO` | `dataFimExecucao` → `dataEntregue` |

| Atributo | Descrição |
|---|---|
| `ordemServicoId`, `codigo` | Identificação da OS |
| `etapa` | Uma das 3 acima |
| `duracaoMs` | Duração da etapa, em milissegundos |

**Alimenta**: dashboard de tempo médio de execução por status.

### `OrdemServicoTransicaoInvalida`
Emitido quando uma transição de status é rejeitada por regra de negócio (ex.: tentar aprovar uma OS
já entregue).

| Atributo | Descrição |
|---|---|
| `ordemServicoId`, `codigo` | Identificação da OS |
| `acao` | Ação que foi tentada (ex.: `"aprovar a ordem de serviço"`) |
| `motivo` | Mensagem de erro completa |

**Alimenta**: dashboard de erros e falhas — nesse caso, falhas de **regra de negócio no
processamento da OS**, não falhas técnicas de infraestrutura (essas últimas já são cobertas
separadamente pelos alarmes do `oficina-lambda` e pela taxa de erro do APM).

## Onde essa instrumentação foi conectada

- `OrdemServicoStatusService.atualizarStatus()` — ponto único por onde passam todas as transições
  reais; emite `OrdemServicoStatusAlterado` e, quando aplicável, `OrdemServicoEtapaConcluida`.
- `OrdemServicoStatusService.validarStatus()` — emite `OrdemServicoTransicaoInvalida` antes de
  lançar a exceção de regra de negócio.
- `OrdemServicoService.criar()` — emite o evento de criação (`OrdemServicoStatusAlterado` com
  `statusAnterior = null`) logo depois do `saveAndFlush` + `refresh`, porque é só nesse ponto que a
  OS já tem `id` (sequence) e `codigo` (trigger do banco).

## Validação

Testado numa implantação real: uma OS (`OS-2026-000001`) foi levada por todo o ciclo de vida
(`RECEBIDA → EM_DIAGNOSTICO → AGUARDANDO_APROVACAO → APROVADA → EM_EXECUCAO → FINALIZADA → ENTREGUE`)
mais uma tentativa de transição inválida. Consulta de confirmação no New Relic:

```sql
SELECT count(*) FROM OrdemServicoStatusAlterado, OrdemServicoEtapaConcluida, OrdemServicoTransicaoInvalida
SINCE 20 minutes ago FACET eventType()
```

Resultado: `OrdemServicoStatusAlterado` = 7, `OrdemServicoEtapaConcluida` = 3,
`OrdemServicoTransicaoInvalida` = 1 — batendo exatamente com as 7 transições e a 1 falha
provocadas no teste.
