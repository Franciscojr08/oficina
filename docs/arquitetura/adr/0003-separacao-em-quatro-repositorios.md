# ADR-0003 — Separação do projeto em quatro repositórios

## Status

Aceito e implementado

## Contexto

Até a Fase 2, um repositório único continha código da aplicação, manifests Kubernetes e o Terraform
de EKS e RDS, com um pipeline que fazia tudo numa execução. A Fase 3 exige quatro repositórios
separados, cada um com CI/CD próprio.

Além da exigência formal, havia um problema concreto: `terraform destroy` acidental derrubava banco
e cluster juntos, e qualquer mudança de aplicação disparava validação de infraestrutura.

## Alternativas consideradas

| Opção | Prós | Contras |
|---|---|---|
| **Quatro repositórios por responsabilidade de infraestrutura** | Atende ao enunciado; ciclos de vida e donos independentes; raio de impacto menor | Nenhum repositório sozinho sobe o sistema; mudança de contrato exige PRs coordenados |
| Monorepo com pipelines por pasta | Menos repositórios para manter; mudança atômica | Não atende ao enunciado, que pede quatro repositórios separados |
| Um repositório por bounded context de negócio (cliente, veículo, OS) | Alinhado ao domínio | Fora de escopo: a fase pede separação por camada de infraestrutura, não por domínio. Exigiria quebrar o monólito |

## Decisão

| Repositório | Responsabilidade |
|---|---|
| `oficina` | Código Spring Boot, migrations Flyway, Dockerfile, pipeline de build e deploy da imagem, documentação arquitetural do sistema |
| `oficina-kubernetes` | Terraform do cluster EKS, manifests da aplicação, configuração do New Relic no cluster |
| `oficina-database` | Terraform do RDS PostgreSQL |
| `oficina-lambda` | Functions `auth-token` e `authorizer`, Terraform do API Gateway |

Os **manifests Kubernetes são mantidos no `oficina-kubernetes`**, junto do cluster que os hospeda e
da configuração de observabilidade que os acompanha.

> **Dívida conhecida:** uma cópia anterior dos manifests ainda existe em `oficina/k8s/aws/`, com 6
> arquivos contra os 8 do `oficina-kubernetes` (que inclui os do New Relic). Ela não é usada pelo
> deploy — a pipeline `app-deploy.yml` só faz `kubectl set image` sobre um Deployment já existente —
> mas permanece no repositório e pode induzir a erro. Remover exige ajustar as seções de deploy do
> README, e ficou de fora do escopo desta entrega.

Nenhum Terraform lê o state de outro. As três ligações entre repositórios são:

| Ligação | Mecanismo |
|---|---|
| `oficina-lambda` → RDS | `data "aws_db_instance"` pelo identificador `oficina-db` |
| `oficina-lambda` → API no EKS | variável `backend_lb_dns`, com o DNS do Service |
| manifests → RDS | `__RDS_ENDPOINT__` substituído no ConfigMap a partir do output do Terraform |

## Justificativa

1. **Ciclos de vida diferentes.** Cluster e banco são bootstrap por ambiente; aplicação e Gateway
   mudam toda semana. Um pipeline único obrigaria os dois ritmos a andarem juntos.
2. **Posse clara.** Cada repositório tem um dono técnico e um histórico próprio.
3. **Acoplamento por leitura, não por state.** Ler o RDS por `data source` em vez de compartilhar
   state significa que um `destroy` em qualquer repositório não corrompe o estado dos outros.

## Consequências

- **Positivas**: deploys independentes; um `apply` errado no Gateway não arrisca o banco; atende
  diretamente ao requisito de segregação.
- **Negativas / trade-offs**:
  - **Nenhum repositório sozinho sobe o sistema.** É preciso seguir a ordem do
    [runbook](https://github.com/rremiao/oficina-kubernetes/blob/main/docs/runbook-ambiente-completo.md):
    banco → cluster → observabilidade → aplicação → Gateway.
  - `backend_lb_dns` é parâmetro manual: o DNS do LoadBalancer muda toda vez que o Service é
    recriado, e o Terraform do Gateway precisa ser reaplicado. É a pegadinha operacional mais comum
    do projeto.
  - Mudança de contrato entre repositórios (uma claim nova no JWT, por exemplo) exige PRs
    coordenados em dois lugares — mitigado documentando o contrato em
    `docs/fase3/contrato-autenticacao.md`.
  - Quatro conjuntos de secrets de CI para manter, o que dói mais no Learner Lab, cujas credenciais
    expiram em 4 horas.
