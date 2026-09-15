# Documentação da arquitetura — Tech Challenge Fase 3

Índice da documentação arquitetural do sistema. Como o projeto é distribuído em quatro
repositórios, **parte da documentação vive no repositório do componente que ela descreve** — este
índice é o mapa completo.

## Diagramas

| Documento | Conteúdo |
| --- | --- |
| [Diagrama de componentes](diagrama-componentes.md) | Visão de nuvem: APIs, banco, serverless e monitoramento, com a origem de cada peça nos quatro repositórios |
| [Sequência — autenticação](diagrama-sequencia-autenticacao.md) | Emissão do token por CPF e consumo de rota protegida, com o Lambda Authorizer e a revalidação na aplicação |
| [Sequência — abertura de ordem de serviço](diagrama-sequencia-abertura-os.md) | Fluxo de criação da OS, geração do código por trigger e baixa de estoque |
| [Modelo ER](https://github.com/rremiao/oficina-database/blob/main/docs/der.md) | Modelo relacional completo e pontos de atenção do schema — vive no `oficina-database` |

## RFCs — decisões técnicas com alternativas comparadas

### Neste repositório

| # | Assunto |
| --- | --- |
| [RFC-0001](rfc/0001-escolha-da-nuvem.md) | Escolha da nuvem: AWS Academy Learner Lab |
| [RFC-0002](rfc/0002-estrategia-de-autenticacao.md) | Estratégia de autenticação do sistema: dois atores, dois emissores, um formato de token |

### Nos demais repositórios

| # | Assunto | Repositório |
| --- | --- | --- |
| [RFC-0001](https://github.com/rremiao/oficina-database/blob/main/docs/rfc/0001-escolha-do-banco-de-dados.md) | Escolha do banco de dados gerenciado | `oficina-database` |
| [RFC-0001](https://github.com/rremiao/oficina-kubernetes/blob/main/docs/rfc/0001-escolha-do-cluster-kubernetes.md) | Escolha do cluster Kubernetes | `oficina-kubernetes` |
| [RFC-0001](https://github.com/rremiao/oficina-lambda/blob/main/docs/rfc/0001-autenticacao-por-cpf.md) | Autenticação por CPF com JWT HS256 | `oficina-lambda` |
| [RFC-0002](https://github.com/rremiao/oficina-lambda/blob/main/docs/rfc/0002-escolha-do-api-gateway.md) | Escolha do API Gateway | `oficina-lambda` |
| [RFC-0003](https://github.com/rremiao/oficina-lambda/blob/main/docs/rfc/0003-runtime-e-empacotamento-da-function.md) | Runtime e empacotamento da function | `oficina-lambda` |
| [RFC-0004](https://github.com/rremiao/oficina-lambda/blob/main/docs/rfc/0004-observabilidade-da-function.md) | Observabilidade da function | `oficina-lambda` |

## ADRs — decisões arquiteturais permanentes

### Neste repositório

| # | Decisão |
| --- | --- |
| [ADR-0001](adr/0001-clean-architecture-ports-and-adapters.md) | Clean Architecture com Ports and Adapters nos módulos de domínio |
| [ADR-0002](adr/0002-flyway-para-versionamento-de-schema.md) | Flyway como fonte de verdade do schema, executado no boot |
| [ADR-0003](adr/0003-separacao-em-quatro-repositorios.md) | Separação do projeto em quatro repositórios |
| [ADR-0004](adr/0004-dois-tipos-de-token-jwt.md) | Dois tipos de token JWT, distinguidos pela claim `tipo` |
| [ADR-0005](adr/0005-observabilidade-da-aplicacao.md) | Logs ECS, correlação de requisições e eventos de negócio |

### Nos demais repositórios

| # | Decisão | Repositório |
| --- | --- | --- |
| [ADR-0001](https://github.com/rremiao/oficina-database/blob/main/docs/adr/0001-segregacao-do-state-de-banco.md) | Segregação do state do banco | `oficina-database` |
| [ADR-0002](https://github.com/rremiao/oficina-database/blob/main/docs/adr/0002-isolamento-de-rede-do-rds.md) | Isolamento de rede do RDS | `oficina-database` |
| [ADR-0003](https://github.com/rremiao/oficina-database/blob/main/docs/adr/0003-dimensionamento-e-ausencia-de-backup.md) | Dimensionamento e ausência de backup | `oficina-database` |
| [ADR-0001](https://github.com/rremiao/oficina-kubernetes/blob/main/docs/adr/0001-estrategia-de-escalabilidade-hpa.md) | Escalabilidade via HPA | `oficina-kubernetes` |
| [ADR-0002](https://github.com/rremiao/oficina-kubernetes/blob/main/docs/adr/0002-escolha-da-ferramenta-de-observabilidade.md) | Escolha da ferramenta de observabilidade | `oficina-kubernetes` |
| [ADR-0003](https://github.com/rremiao/oficina-kubernetes/blob/main/docs/adr/0003-licoes-da-primeira-implantacao-real.md) | Lições da primeira implantação real | `oficina-kubernetes` |
| [ADR-0004](https://github.com/rremiao/oficina-kubernetes/blob/main/docs/adr/0004-dashboards-alertas-e-synthetic-monitor.md) | Dashboards, alertas e Synthetic Monitor | `oficina-kubernetes` |
| [ADR-0001](https://github.com/rremiao/oficina-lambda/blob/main/docs/adr/0001-dupla-validacao-jwt.md) | Dupla validação do JWT | `oficina-lambda` |
| [ADR-0002](https://github.com/rremiao/oficina-lambda/blob/main/docs/adr/0002-comunicacao-sincrona-via-api-gateway.md) | Comunicação síncrona via API Gateway | `oficina-lambda` |
| [ADR-0003](https://github.com/rremiao/oficina-lambda/blob/main/docs/adr/0003-logs-estruturados-e-correlacao.md) | Logs estruturados e correlação | `oficina-lambda` |
| [ADR-0004](https://github.com/rremiao/oficina-lambda/blob/main/docs/adr/0004-escalabilidade-concorrencia-reservada.md) | Escalabilidade por concorrência reservada | `oficina-lambda` |
| [ADR-0005](https://github.com/rremiao/oficina-lambda/blob/main/docs/adr/0005-segredos-em-variaveis-de-ambiente.md) | Segredos em variáveis de ambiente | `oficina-lambda` |
| [ADR-0006](https://github.com/rremiao/oficina-lambda/blob/main/docs/adr/0006-isolamento-de-rede.md) | Isolamento de rede das functions | `oficina-lambda` |
| [ADR-0007](https://github.com/rremiao/oficina-lambda/blob/main/docs/adr/0007-instrumentacao-new-relic-via-layer.md) | Instrumentação via New Relic Lambda Layer | `oficina-lambda` |

## Documentos relacionados

| Documento | Onde |
| --- | --- |
| [Contrato de autenticação](../fase3/contrato-autenticacao.md) | Contrato entre esta API e o `oficina-lambda`: claims, códigos de resposta e o segredo compartilhado |
| [Eventos customizados](../observabilidade/eventos-customizados.md) | Logs estruturados e eventos de negócio que alimentam os dashboards |
| [Runbook de ambiente completo](https://github.com/rremiao/oficina-kubernetes/blob/main/docs/runbook-ambiente-completo.md) | Passo a passo para subir os quatro repositórios na AWS |
| [Collection Postman](https://github.com/rremiao/oficina-lambda/tree/main/docs/postman) | Verificação do fluxo de autenticação ponta a ponta |

## Onde cada requisito do enunciado é atendido

| Requisito | Onde está documentado |
| --- | --- |
| Diagrama de componentes | [diagrama-componentes.md](diagrama-componentes.md) |
| Diagrama de sequência — autenticação | [diagrama-sequencia-autenticacao.md](diagrama-sequencia-autenticacao.md) |
| Diagrama de sequência — abertura de OS | [diagrama-sequencia-abertura-os.md](diagrama-sequencia-abertura-os.md) |
| RFCs para decisões técnicas | 2 neste repositório + 6 nos demais (tabelas acima) |
| ADRs para decisões arquiteturais | 5 neste repositório + 14 nos demais (tabelas acima) |
| Justificativa do banco + modelo ER | [RFC-0001](https://github.com/rremiao/oficina-database/blob/main/docs/rfc/0001-escolha-do-banco-de-dados.md) e [der.md](https://github.com/rremiao/oficina-database/blob/main/docs/der.md) do `oficina-database` |
