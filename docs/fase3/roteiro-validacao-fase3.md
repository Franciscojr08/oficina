# Roteiro de subida e validação — Tech Challenge Fase 3

Como subir os quatro componentes na AWS e **comprovar**, um a um, cada requisito do enunciado.
Serve para a validação antes da entrega e como roteiro de gravação do vídeo.

O passo a passo detalhado de provisionamento, com o porquê de cada ordem, está no
[runbook de ambiente completo](https://github.com/rremiao/oficina-kubernetes/blob/main/docs/runbook-ambiente-completo.md)
do `oficina-kubernetes`. Aqui o foco é a **verificação**.

---

## Parte 0 — Pré-requisitos

- Sessão do **AWS Academy Learner Lab** ativa (credenciais válidas por 4h)
- Conta **New Relic** com a *Ingest License Key* (a linha `INGEST - LICENSE`, não a `NRAK-` nem a `NRJS-`)
- `aws`, `terraform`, `kubectl`, `helm`, `docker`, `jq`, `node` e `python3` instalados
- Bucket S3 do state do `oficina-lambda` criado (`oficina-lambda-tfstate-<account-id>`) — o Terraform
  não cria o próprio backend

```bash
aws sts get-caller-identity          # confirma a sessão
export ACCOUNT=$(aws sts get-caller-identity --query Account --output text)
export REGION=us-east-1
```

> **Atenção antes de começar:** confirme que a `main` do `oficina` contém `TipoToken.java`,
> `ClienteAutenticado.java` e a migration `V10`. Sem elas o Gateway autoriza o token de cliente mas a
> aplicação o rejeita, e o requisito de autenticação por CPF falha no meio do caminho.
>
> ```bash
> git -C oficina ls-tree -r --name-only origin/main | grep -E "TipoToken|ClienteAutenticado|V10__"
> ```

---

## Parte 1 — Subir o ambiente

Ordem obrigatória (cada passo depende do anterior):

| # | Componente | Comando principal | Saída que você precisa guardar |
| --- | --- | --- | --- |
| 1 | RDS (`oficina-database/infra`) | `terraform apply` | `rds_endpoint` |
| 2 | EKS (`oficina-kubernetes/infra`) | `terraform apply` | nome do cluster |
| 3 | kubeconfig | `aws eks update-kubeconfig --region $REGION --name oficina-eks` | — |
| 4 | New Relic no cluster | `helm install` do `nri-bundle` + `kubectl apply` de `observability/` | — |
| 5 | Aplicação | `kubectl apply -f k8s/` (com `__RDS_ENDPOINT__` substituído) | `LB_DNS` do Service |
| 6 | Integração Postgres | usuário `newrelic_monitor` + `k8s/07-newrelic-postgresql.yaml` | — |
| 7 | API Gateway + Lambdas (`oficina-lambda/infra`) | `terraform apply` com `backend_lb_dns=$LB_DNS` | `api_endpoint` |

```bash
export RDS_ENDPOINT=<output do passo 1>
export LB_DNS=$(kubectl get svc oficina-api -n oficina -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
export API=$(cd oficina-lambda/infra && terraform output -raw api_endpoint | sed 's#/$##')
```

**`node_desired_size` precisa ser ≥ 2.** O bundle do New Relic (7 pods) mais os pods de sistema do
EKS estouram o limite de uma `t3.small` sozinha.

### Semear os dados de teste

O RDS é privado; o `psql` roda num pod efêmero dentro do cluster.

```bash
kubectl run psql-seed -n oficina --rm -i --restart=Never --image=postgres:16 \
  --env=PGPASSWORD="$DB_PASS" --command -- \
  psql -h "$RDS_ENDPOINT" -U "$DB_USER" -d oficina -v ON_ERROR_STOP=1 -c "
INSERT INTO cliente (nome,cpf_cnpj,cep,logradouro,bairro,cidade,uf,data_nascimento,ativo)
VALUES ('Cliente E2E Ativo','529.982.247-25','01001000','Praca da Se','Se','Sao Paulo','SP','1990-01-01',TRUE)
ON CONFLICT (cpf_cnpj) DO UPDATE SET ativo=TRUE;
INSERT INTO cliente (nome,cpf_cnpj,cep,logradouro,bairro,cidade,uf,data_nascimento,ativo)
VALUES ('Cliente E2E Inativo','11144477735','20040002','Av Rio Branco','Centro','Rio de Janeiro','RJ','1985-05-05',FALSE)
ON CONFLICT (cpf_cnpj) DO UPDATE SET ativo=FALSE;" | cat
```

Os dois CPFs são gravados em formatos diferentes de propósito — um com máscara, outro sem. É o que
prova que a normalização da function e o índice `V10` funcionam nos dois casos.

| CPF | Situação | Resposta esperada |
| --- | --- | --- |
| `529.982.247-25` | ativo, com máscara | `200` |
| `111.444.777-35` | inativo, sem máscara | `403` |
| `390.533.447-05` | válido, sem cliente | `404` |
| `111.111.111-11` | dígitos repetidos | `400` |

---

## Parte 2 — Verificação requisito a requisito

### 1. Autenticação e API Gateway

| Requisito | Como comprovar |
| --- | --- |
| Implementar um API Gateway | `aws apigatewayv2 get-apis --query "Items[?Name=='oficina-hml-api']"` — ou o console, mostrando as rotas |
| Function Serverless valida CPF, consulta cliente e devolve JWT | Os quatro `curl` de `/auth/token` abaixo, um por cenário |
| Rotas sensíveis protegidas por autenticação via CPF | Rota protegida com token (`200`), sem token (`401`) e com token adulterado (`403`) |

```bash
# emissão — os quatro cenários
curl -s -X POST "$API/auth/token" -H 'content-type: application/json' -d '{"cpf":"529.982.247-25"}' | jq .
for cpf in 111.111.111-11 390.533.447-05 111.444.777-35; do
  printf "%s -> " "$cpf"
  curl -s -o /dev/null -w '%{http_code}\n' -X POST "$API/auth/token" \
    -H 'content-type: application/json' -d "{\"cpf\":\"$cpf\"}"
done                                     # esperado: 400, 404, 403

RESP=$(curl -s -X POST "$API/auth/token" -H 'content-type: application/json' -d '{"cpf":"529.982.247-25"}')
TOKEN=$(echo "$RESP" | jq -r .token); CID=$(echo "$RESP" | jq -r .cliente.id)

# rota protegida
curl -s -o /dev/null -w 'com token:       %{http_code}\n' -H "Authorization: Bearer $TOKEN" "$API/oficina/v1/ordens/cliente/$CID"
curl -s -o /dev/null -w 'sem token:       %{http_code}\n' "$API/oficina/v1/ordens/cliente/$CID"
curl -s -o /dev/null -w 'token adulterado:%{http_code}\n' -H "Authorization: Bearer ${TOKEN}xx" "$API/oficina/v1/ordens/cliente/$CID"
```

A [collection Postman](https://github.com/rremiao/oficina-lambda/tree/main/docs/postman) roda esses
mesmos casos com asserções automáticas — é a forma mais limpa de mostrar no vídeo.

**Prova extra da dupla validação** (vale mostrar): chame a API por fora do Gateway, direto no
LoadBalancer, com um token adulterado. Ela recusa sozinha, sem authorizer no caminho.

```bash
curl -s -o /dev/null -w '%{http_code}\n' -H "Authorization: Bearer ${TOKEN}xx" \
  "http://${LB_DNS}/oficina/v1/ordens/cliente/${CID}"
```

### 2. Estrutura de repositórios e CI/CD

| Requisito | Como comprovar |
| --- | --- |
| Quatro repositórios separados | Os quatro no GitHub, cada um com README, `docs/` e workflow |
| CI/CD em cada repositório | Aba **Actions** de cada repo: `app-deploy.yml`, `ci.yml` + `deploy.yml`, `infra-database.yml`, `infra-kubernetes.yml` |
| Branch protegida, só via PR | Settings → Branches de cada repo, mostrando a regra |
| Deploy automático homolog/produção | `oficina-lambda`: push em `homolog` → `hml`, push em `main` → `prd`. Nos repos de infra, o workflow valida o Terraform — a limitação está registrada e deve ser explicada no vídeo |

### 3. Infraestrutura obrigatória

| Requisito | Como comprovar |
| --- | --- |
| API Gateway | `terraform output api_endpoint`, e o console mostrando as rotas e o authorizer |
| Function Serverless | `aws lambda list-functions --query "Functions[?starts_with(FunctionName,'oficina-')].FunctionName"` |
| Banco gerenciado | `aws rds describe-db-instances --db-instance-identifier oficina-db --query 'DBInstances[0].[DBInstanceStatus,Engine,PubliclyAccessible]'` |
| Cluster Kubernetes com escalabilidade | `kubectl get hpa -n oficina` mostrando `1/3` e o alvo de CPU |
| Terraform | Os três `infra/` versionados, e o state do `oficina-lambda` no S3 |

```bash
kubectl get hpa,deploy,svc,pods -n oficina
```

Para demonstrar o HPA agindo ao vivo, gere carga e acompanhe:

```bash
kubectl run carga -n oficina --rm -i --restart=Never --image=busybox -- \
  sh -c 'while true; do wget -q -O- http://oficina-api/oficina/v1/api-docs >/dev/null; done' &
kubectl get hpa -n oficina -w
```

### 4. Monitoramento e observabilidade

| Requisito | Onde mostrar no New Relic |
| --- | --- |
| Integração com New Relic | Entidades `oficina` (APM), cluster e RDS aparecendo |
| Latência das APIs | APM → Summary, gráfico de response time |
| CPU e memória do Kubernetes | Kubernetes → cluster explorer |
| Healthchecks e uptime | Synthetics → o monitor configurado |
| Alertas de falha no processamento de OS | Alerts → a condition e o histórico de disparo |
| Logs estruturados com correlação | Logs, filtrando por `correlationId` |
| Dashboard — volume diário de OS | Dashboards |
| Dashboard — tempo médio por status | Dashboards |
| Dashboard — erros nas integrações | Dashboards |

**Correlação ponta a ponta** — o mesmo identificador nos três pontos:

```bash
CORR="demo-$(date +%s)"
curl -s -o /dev/null -H "Authorization: Bearer $TOKEN" -H "x-correlation-id: $CORR" \
  "$API/oficina/v1/ordens/cliente/$CID"
sleep 20
INICIO=$(( ($(date +%s)-300)*1000 ))
aws logs filter-log-events --log-group-name /aws/apigateway/oficina-hml --start-time $INICIO | grep "$CORR"
aws logs filter-log-events --log-group-name /aws/lambda/oficina-hml-authorizer --start-time $INICIO | grep "$CORR"
kubectl logs -n oficina -l app.kubernetes.io/name=oficina-api --tail=500 | grep "$CORR" | cat
```

Confirme também que **nenhum CPF aparece em claro** em nenhum dos três: na function ele sai como
hash truncado, e na aplicação o filtro mascara sequências de 8 ou mais dígitos na rota.

Para alimentar os dashboards antes da gravação, faça uma OS percorrer o ciclo completo — criar,
iniciar diagnóstico, solicitar aprovação, aprovar, iniciar execução, entregar:

```bash
curl -s -X POST "$API/oficina/v1/ordens" -H "Authorization: Bearer $TOKEN_OPERADOR" \
  -H 'content-type: application/json' -d '{...}'
# e os PATCH de transição de status em /ordens/{id}/...
```

### 5. Documentação da arquitetura

| Requisito | Onde está |
| --- | --- |
| Diagrama de componentes | [`docs/arquitetura/diagrama-componentes.md`](../arquitetura/diagrama-componentes.md) |
| Sequência — autenticação | [`docs/arquitetura/diagrama-sequencia-autenticacao.md`](../arquitetura/diagrama-sequencia-autenticacao.md) |
| Sequência — abertura de OS | [`docs/arquitetura/diagrama-sequencia-abertura-os.md`](../arquitetura/diagrama-sequencia-abertura-os.md) |
| RFCs | 2 aqui + 6 nos outros repositórios — índice em [`docs/arquitetura/README.md`](../arquitetura/README.md) |
| ADRs | 5 aqui + 14 nos outros repositórios — mesmo índice |
| Justificativa do banco + ER | [RFC-0001](https://github.com/rremiao/oficina-database/blob/main/docs/rfc/0001-escolha-do-banco-de-dados.md) e [der.md](https://github.com/rremiao/oficina-database/blob/main/docs/der.md) |

### 6. Entregável — repositórios

| Requisito | Como comprovar |
| --- | --- |
| README claro em cada repo | Os quatro têm propósito, tecnologias, passos de execução, diagrama próprio e link de Swagger/Postman |
| Dockerfiles onde aplicável | `oficina/Dockerfile` (a Lambda usa zip, os de infra não têm imagem) |
| Pipelines funcionais | Execuções verdes na aba Actions |
| Links para deploys ativos | Não se aplica: o ambiente do Academy é efêmero, sem URL permanente |

### 7. Entregável — vídeo e Portal

Ordem sugerida de gravação (≤ 15 min):

1. **Os quatro repositórios** (1 min) — estrutura, README, docs, Actions verde
2. **Pipeline CI/CD** (2 min) — um push disparando build e deploy
3. **Deploy automatizado** (2 min) — `kubectl get all -n oficina`, rollout concluído
4. **Autenticação com CPF** (3 min) — a collection Postman rodando: os quatro cenários de emissão e
   os três de rota protegida
5. **Consumo das APIs protegidas** (2 min) — abertura de OS com o token, e a resposta `201`
6. **Dashboards** (3 min) — os três dashboards com dado real, e o alerta com histórico de disparo
7. **Logs e traces** (2 min) — o mesmo `correlationId` no Gateway, no authorizer e na aplicação

Para o PDF do Portal do Aluno: links dos quatro repositórios, link do vídeo, link desta documentação
e confirmação de que `soat-architecture` foi adicionado como colaborador nos quatro repos.

---

## Parte 3 — Derrubar o ambiente

Ordem inversa. O Service precisa sair **antes** do cluster, senão o LoadBalancer fica órfão
consumindo crédito.

```bash
kubectl delete svc oficina-api -n oficina
cd oficina-lambda/infra     && terraform destroy -auto-approve && cd -
cd oficina-kubernetes/infra && terraform destroy -auto-approve && cd -
cd oficina-database/infra   && terraform destroy -auto-approve && cd -
```

Confirme que nada ficou de pé:

```bash
aws elbv2 describe-load-balancers --query 'LoadBalancers[].LoadBalancerName'
aws rds describe-db-instances --query 'DBInstances[].DBInstanceIdentifier'
aws eks list-clusters
```

---

## Armadilhas conhecidas

| Sintoma | Causa |
| --- | --- |
| Token emitido, mas rota protegida devolve `403` | `jwt_secret` da Lambda diferente do `SECURITY_JWT_SECRET` do Secret do k8s |
| Token emitido, mas rota protegida devolve `500` | A `main` do `oficina` está sem o suporte a token de cliente (`TipoToken`) |
| Rota protegida devolve `404` no proxy | `backend_lb_dns` desatualizado — o DNS muda quando o Service é recriado |
| `terraform apply` da Lambda falha na integração | `backend_lb_dns` com TLD inválido, ou ainda com o valor `SUBSTITUIR-...` |
| Todo `/auth/token` devolve `503` | Faltou `-var="db_username=oficina_user"` (o default é `postgres`), ou o security group do RDS não alcança a subnet da Lambda |
| Pods do New Relic em `Pending` | `node_desired_size = 1` — o bundle não cabe numa `t3.small` sozinha |
| HPA com `TARGETS: <unknown>` | Falta o `metrics-server` no cluster |
| `kubectl` não imprime nada e sai 0 | `kubectl` via snap engolindo stdout — encadeie `\| cat` |
| Credencial expira no meio | Sessão do Learner Lab dura 4h — refaça `~/.aws/credentials` e continue |
