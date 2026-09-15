# ADR-0001 — Clean Architecture com Ports and Adapters nos módulos de domínio

## Status

Aceito — decisão herdada das fases anteriores, mantida na Fase 3

## Contexto

A aplicação começou como um CRUD de oficina e cresceu para um domínio com regras próprias: ciclo de
vida da ordem de serviço, controle de estoque, validação de documento. Na Fase 3 ela passou a ser
consumida também por um componente externo (a function de autenticação por CPF), o que reforçou a
necessidade de fronteiras claras entre regra de negócio e detalhe de infraestrutura.

## Alternativas consideradas

| Opção | Prós | Contras |
|---|---|---|
| **Clean Architecture / Ports and Adapters por módulo de domínio** | Regra de negócio não depende de Spring nem de JPA; troca de adapter não toca no caso de uso; cada módulo é legível isoladamente | Mais arquivos e indireção para operações simples; exige disciplina do time |
| Camadas técnicas clássicas (`controller` / `service` / `repository` na raiz) | Menos arquivos; familiar | Agrupa por tecnologia, não por domínio: o código de uma regra fica espalhado por três pacotes distantes |
| Domínio anêmico com lógica nos services | Rápido de escrever | A entidade vira estrutura de dados e a regra vaza para várias classes |

## Decisão

Cada módulo de domínio é organizado por camadas internas, não por tipo técnico:

```
<modulo>/
├── domain/                    entidades e enums
├── application/               regras de negócio, DTOs e portas
│   ├── usecase/               contratos de entrada, usados pelos controllers
│   └── gateway/               portas de saída (persistência, integrações)
├── entrypoint/controller/     adapters de entrada HTTP/REST
└── output/persistence/        adapters de saída, com Spring Data/JPA
```

Vale para `cliente`, `veiculo`, `servico`, `item`, `estoque`, `movimentoestoque`, `ordemservico` e
`auth.gestaousuarios`.

Os pacotes `shared`, `security`, `config`, `relatorio` e `apipublica` têm organização própria por
tratarem preocupação transversal ou fluxo específico — e isso é intencional, não descuido.

## Justificativa

1. **A dependência aponta para dentro.** `application` não conhece Spring Data; ela declara a porta
   (`gateway`) e o adapter em `output/persistence` a implementa. Trocar JPA por outra coisa não toca
   na regra.
2. **O módulo é a unidade de leitura.** Quem vai mexer em ordem de serviço abre uma pasta e encontra
   entidade, regra, contrato de entrada e persistência — sem navegar por três pacotes técnicos.
3. **Facilitou a Fase 3.** A adição do token de cliente (ADR-0004) ficou contida em `security`, sem
   tocar em nenhum caso de uso de domínio.

## Consequências

- **Positivas**: regra de negócio testável sem subir contexto Spring; fronteiras explícitas entre
  domínio e infraestrutura; módulos evoluem de forma independente.
- **Negativas / trade-offs**:
  - Mais arquivos por operação. Para um CRUD simples, a indireção não se paga — e parte do código
    deste projeto é CRUD simples.
  - A convenção só vale se for seguida. Os pacotes transversais já fogem do padrão por bons motivos,
    mas cada exceção nova exige justificativa, senão a estrutura vira decoração.
  - A separação é lógica, não física: nada no build impede `application` importar `output`. Só
    revisão de código garante a regra.
