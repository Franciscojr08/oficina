# Deploy AWS com EKS + RDS

Este guia descreve o deploy da Oficina API no AWS Academy/Learner Lab usando EKS, RDS PostgreSQL privado e Service Kubernetes `LoadBalancer`.

Arquitetura:

```text
Usuario
  -> AWS Load Balancer publico
      -> Kubernetes Service oficina-api
          -> Pod oficina-api no EKS
              -> RDS PostgreSQL privado
```

O fluxo local com kind em `infra/` e manifests locais em `k8s/` continua separado. O deploy AWS usa `infra/aws/` e `k8s/aws/`.

## Pre-requisitos

- AWS Academy/Learner Lab iniciado.
- AWS CLI configurado com profile `academy`.
- Terraform instalado.
- `kubectl` instalado.
- Docker instalado.
- Login no Docker Hub configurado.
- Credenciais temporarias do Lab atualizadas sempre que o Lab reiniciar.

> As credenciais do AWS Academy/Learner Lab sao temporarias. Atualize `~/.aws/credentials` localmente ou os secrets do GitHub Actions sempre que iniciar uma nova sessao do Lab.

## Validacao inicial do AWS Lab

```bash
aws sts get-caller-identity --profile academy
aws eks list-clusters --region us-east-1 --profile academy
aws rds describe-db-instances --region us-east-1 --profile academy
aws ec2 describe-vpcs --region us-east-1 --profile academy
```

## Build e push da imagem

A imagem validada para o Lab atual e:

```text
anthonymeds/oficina-api:aws-v1
```

Neste Lab a publicacao usa Docker Hub. Nao usamos ECR porque o AWS Academy/Learner Lab bloqueou permissoes de push no ECR.

Para recriar e publicar:

```bash
docker login
docker build -t anthonymeds/oficina-api:aws-v1 .
docker push anthonymeds/oficina-api:aws-v1
```

## Criar infraestrutura AWS

O Terraform AWS usa:

- VPC default e subnets default como base.
- Subnets do EKS apenas em `us-east-1a`, `us-east-1b`, `us-east-1c`, `us-east-1d` e `us-east-1f`.
- RDS PostgreSQL privado com `publicly_accessible=false`.
- Security Group do RDS liberando `5432` apenas dentro do CIDR da VPC.
- EKS `oficina-eks` com role `LabRole`.
- Node group `oficina-node-group` com `t3.small`, desired/min/max `1/1/2`.

A AZ `us-east-1e` fica fora das subnets do EKS porque o control plane retornou `UnsupportedAvailabilityZoneException` nesse Learner Lab. O RDS continua usando as subnets default.

Defina a senha do RDS antes do plan/apply. No Bash:

```bash
export TF_VAR_db_password='Oficina12345!'
```

No PowerShell:

```powershell
$env:TF_VAR_db_password='Oficina12345!'
```

Execute:

```bash
terraform -chdir=infra/aws init
terraform -chdir=infra/aws fmt
terraform -chdir=infra/aws validate
terraform -chdir=infra/aws plan
terraform -chdir=infra/aws apply
```

Nao versione `terraform.tfvars`, `*.tfstate`, kubeconfig ou arquivos em `k8s/aws/generated/`.

## Configurar kubectl para o EKS

```bash
aws eks update-kubeconfig --region us-east-1 --name oficina-eks --profile academy
kubectl get nodes
```

## Instalar metrics-server

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
kubectl get deployment metrics-server -n kube-system
kubectl top nodes
```

## Gerar ConfigMap com endpoint do RDS

```bash
RDS_ENDPOINT=$(aws rds describe-db-instances \
  --db-instance-identifier oficina-db \
  --region us-east-1 \
  --profile academy \
  --query 'DBInstances[0].Endpoint.Address' \
  --output text)

mkdir -p k8s/aws/generated

sed "s|__RDS_ENDPOINT__|$RDS_ENDPOINT|g" \
  k8s/aws/01-configmap.yaml.tpl \
  > k8s/aws/generated/01-configmap.yaml
```

## Aplicar manifests AWS

```bash
kubectl apply -f k8s/aws/00-namespace.yaml
kubectl apply -f k8s/aws/generated/01-configmap.yaml
kubectl apply -f k8s/aws/02-secret.yaml
kubectl apply -f k8s/aws/03-app-deployment.yaml
kubectl apply -f k8s/aws/04-app-service-loadbalancer.yaml
kubectl apply -f k8s/aws/05-hpa.yaml
```

O arquivo `k8s/aws/02-secret.yaml` existe para simplicidade academica do Lab. Em CI/CD, a Secret Kubernetes e gerada a partir de GitHub Secrets.

## Validar aplicacao

```bash
kubectl get pods -n oficina
kubectl logs -f deployment/oficina-api -n oficina
kubectl get svc oficina-api -n oficina
kubectl get hpa -n oficina
```

## Obter URL publica

```bash
LB_DNS=$(kubectl get svc oficina-api -n oficina -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
echo $LB_DNS
curl "http://$LB_DNS/oficina/v1/api-docs"
```

Swagger:

```text
http://$LB_DNS/oficina/v1/swagger-ui.html
```

OpenAPI:

```text
http://$LB_DNS/oficina/v1/api-docs
```



Ao finalizar:

```bash
kubectl delete pod rds-tunnel -n oficina --ignore-not-found
```

## CI/CD AWS manual

O workflow AWS fica em `.github/workflows/aws-deploy.yml` e roda apenas por `workflow_dispatch`, com input:

- `deploy`: build/test/package, push Docker Hub, Terraform apply, deploy no EKS e smoke test.
- `destroy`: remove manifests Kubernetes primeiro e depois executa Terraform destroy.

Secrets necessarios no GitHub:

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_SESSION_TOKEN
AWS_REGION=us-east-1
DOCKERHUB_USERNAME=anthonymeds
DOCKERHUB_TOKEN
DB_PASSWORD=Oficina12345!
SECURITY_JWT_SECRET
```

Como o AWS Academy/Learner Lab usa credenciais temporarias, o workflow so funciona enquanto `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` e `AWS_SESSION_TOKEN` estiverem atualizados com a sessao atual do Lab.

## Como destruir o ambiente AWS no final do Lab

EKS, EC2 node group, RDS, Load Balancer, EBS e snapshots podem consumir credito. Execute o destroy ao final dos testes.

```bash
# Remover recursos Kubernetes que criam Load Balancer
kubectl delete -f k8s/aws/05-hpa.yaml --ignore-not-found
kubectl delete -f k8s/aws/04-app-service-loadbalancer.yaml --ignore-not-found
kubectl delete -f k8s/aws/03-app-deployment.yaml --ignore-not-found
kubectl delete -f k8s/aws/02-secret.yaml --ignore-not-found
kubectl delete -f k8s/aws/generated/01-configmap.yaml --ignore-not-found
kubectl delete -f k8s/aws/00-namespace.yaml --ignore-not-found

# Aguardar remocao do Load Balancer
aws elbv2 describe-load-balancers --region us-east-1 --profile academy
aws elb describe-load-balancers --region us-east-1 --profile academy

# Destruir infraestrutura Terraform
terraform -chdir=infra/aws destroy

# Conferencias finais
aws eks list-clusters --region us-east-1 --profile academy
aws rds describe-db-instances --region us-east-1 --profile academy
aws ec2 describe-instances \
  --region us-east-1 \
  --profile academy \
  --filters Name=instance-state-name,Values=running,pending \
  --query 'Reservations[*].Instances[*].[InstanceId,InstanceType,State.Name,Tags]' \
  --output table

aws elbv2 describe-load-balancers --region us-east-1 --profile academy
aws elb describe-load-balancers --region us-east-1 --profile academy
```
