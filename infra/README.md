

A infraestrutura local usa:

- Docker Desktop
- kubectl
- kind
- Terraform
- Kubernetes manifests na pasta /k8s
- PostgreSQL dentro do Kubernetes
- PVC para persistência do banco
- ConfigMap para configurações não sensíveis
- Secret para credenciais locais
- Deployment da API
- Services internos
- HPA para escalabilidade horizontal
- port-forward para acessar o Swagger localmente

Fluxo geral:

Terraform
-> cria o cluster Kubernetes local com kind

Docker
-> builda a imagem da API: oficina-api:local

kind
-> carrega a imagem local dentro do cluster Kubernetes

kubectl
-> aplica os manifests da pasta /k8s

Kubernetes
-> sobe PostgreSQL, API, Services, PVC, ConfigMap, Secret e HPA


============================================================
2. ARQUITETURA LOCAL
   ============================================================

Máquina local
-> Docker Desktop
-> kind
-> Cluster Kubernetes local: techchallenge
-> Namespace: oficina
-> Pod oficina-api
-> Service oficina-api
-> HPA oficina-api-hpa
-> Pod postgres-0
-> Service postgres
-> PVC postgres-data
-> ConfigMap oficina-config
-> Secret oficina-secret


============================================================
3. FERRAMENTAS NECESSÁRIAS
   ============================================================

Instalar:

1. Docker Desktop
2. kubectl
3. kind
4. Terraform
5. Opcional: k9s para visualizar o cluster no terminal

No Windows, pode instalar com winget:

winget install Kubernetes.kubectl
winget install Kubernetes.kind
winget install HashiCorp.Terraform
winget install k9s

Depois de instalar, feche e abra novamente o terminal para atualizar o PATH.

Validar instalações:

docker version
kubectl version --client
kind version
terraform version

Se algum comando não for reconhecido, feche e abra o terminal novamente.


============================================================
4. PASTAS DA INFRA
   ============================================================

A infraestrutura usa duas pastas principais:

infra/
-> contém Terraform
-> cria o cluster Kubernetes local com kind

k8s/
-> cria os recursos dentro do cluster Kubernetes

Ordem correta:

1. terraform apply
2. docker build
3. kind load docker-image
4. kubectl apply -f k8s


============================================================
5. ARQUIVOS DA PASTA infra/
   ============================================================

infra/providers.tf
-> define o provider kind usado pelo Terraform.

infra/main.tf
-> cria o cluster kind local.

infra/variables.tf
-> define variáveis, como o nome do cluster.

Nome padrão do cluster:

techchallenge

infra/outputs.tf
-> mostra outputs úteis após o apply, como:
- cluster_name
- kubectl_context
- próximos comandos

infra/.terraform.lock.hcl
-> arquivo de lock do Terraform.
-> pode ser versionado para garantir mesma versão do provider.

infra/terraform.tfstate
-> arquivo de estado local do Terraform.
-> registra o que foi criado.


============================================================
6. ARQUIVOS DA PASTA k8s/
   ============================================================

k8s/00-namespace.yaml
-> cria o namespace oficina.

k8s/01-configmap.yaml
-> cria o ConfigMap oficina-config.
-> guarda configurações não sensíveis

k8s/02-secret.yaml
-> cria o Secret oficina-secret.
-> guarda credenciais locais:

k8s/03-postgres-pvc.yaml
-> cria o PVC postgres-data.
-> PVC significa PersistentVolumeClaim.
-> funciona como o disco persistente do PostgreSQL.

k8s/04-postgres-statefulset.yaml
-> sobe o PostgreSQL dentro do Kubernetes.
-> usa imagem postgres:16.
-> monta o PVC em /var/lib/postgresql/data.
-> cria o Pod postgres-0.

k8s/05-postgres-service.yaml
-> cria o Service postgres.
-> expõe o PostgreSQL internamente no cluster.
-> a API conecta usando:
jdbc:postgresql://postgres:5432/oficina

k8s/06-app-deployment.yaml
-> sobe a API Spring Boot.
-> usa imagem oficina-api:local.
-> porta 8080.
-> usa ConfigMap e Secret.
-> define resources requests/limits.
-> define readinessProbe e livenessProbe em:
/oficina/v1/api-docs

k8s/07-app-service.yaml
-> cria o Service oficina-api.
-> tipo ClusterIP.
-> expõe a API internamente no cluster.

k8s/08-hpa.yaml
-> cria o HPA oficina-api-hpa.
-> HPA significa Horizontal Pod Autoscaler.
-> minReplicas=1.
-> maxReplicas=3.
-> target CPU=70%.



============================================================
9. CRIAR CLUSTER KIND COM TERRAFORM
   ============================================================

Inicializar Terraform:

terraform -chdir=infra init

Validar configuração:

terraform -chdir=infra validate

Resultado esperado:

Success! The configuration is valid.

Ver plano:

terraform -chdir=infra plan

No plan deve aparecer:

name       = "techchallenge"
node_image = "kindest/node:v1.30.0"

Criar cluster:

terraform -chdir=infra apply -auto-approve

Resultado esperado:

Apply complete! Resources: 1 added, 0 changed, 0 destroyed.

Validar cluster:

kind get clusters

Resultado esperado:

techchallenge

Selecionar contexto kubectl:

kubectl config use-context kind-techchallenge

Validar node:

kubectl get nodes

Resultado esperado:

NAME                          STATUS   ROLES           VERSION
techchallenge-control-plane   Ready    control-plane   v1.30.0


============================================================
10. BUILDAR IMAGEM DOCKER DA API
    ============================================================

Na raiz do projeto, executar:

docker build -t oficina-api:local .

Esse comando usa o Dockerfile e cria a imagem local:

oficina-api:local

Validar imagem:

docker images | findstr oficina-api

Resultado esperado:

oficina-api   local


============================================================
11. CARREGAR IMAGEM NO CLUSTER KIND
    ============================================================

O kind não enxerga automaticamente as imagens locais do Docker.

Por isso, carregar a imagem no cluster:

kind load docker-image oficina-api:local --name techchallenge

Resultado esperado:

Image: "oficina-api:local" ... loading...

Esse passo evita erro:

ImagePullBackOff
ErrImagePull


============================================================
12. APLICAR RECURSOS BASE DO KUBERNETES
    ============================================================

Aplicar namespace:

kubectl apply -f k8s/00-namespace.yaml

Aplicar ConfigMap:

kubectl apply -f k8s/01-configmap.yaml

Aplicar Secret:

kubectl apply -f k8s/02-secret.yaml

Validar:

kubectl get namespaces
kubectl get configmap -n oficina
kubectl get secret -n oficina


============================================================
13. SUBIR POSTGRESQL NO KUBERNETES
    ============================================================

Aplicar PVC:

kubectl apply -f k8s/03-postgres-pvc.yaml

Aplicar Service do PostgreSQL:

kubectl apply -f k8s/05-postgres-service.yaml

Aplicar StatefulSet do PostgreSQL:

kubectl apply -f k8s/04-postgres-statefulset.yaml

Validar recursos:

kubectl get pods -n oficina
kubectl get pvc -n oficina
kubectl get svc -n oficina

Resultado esperado:

postgres-0   1/1   Running

PVC esperado:

postgres-data   Bound

Service esperado:

postgres   ClusterIP   5432/TCP

Validar se o PostgreSQL está aceitando conexão:

kubectl exec -n oficina postgres-0 -- pg_isready -U postgres -d oficina

Resultado esperado:

/var/run/postgresql:5432 - accepting connections


============================================================
14. SUBIR API E HPA
    ============================================================

Aplicar Deployment da API:

kubectl apply -f k8s/06-app-deployment.yaml

Aplicar Service da API:

kubectl apply -f k8s/07-app-service.yaml

Aplicar HPA:

kubectl apply -f k8s/08-hpa.yaml

Validar todos os recursos:

kubectl get all -n oficina

Resultado esperado:

pod/oficina-api-...   1/1   Running
pod/postgres-0        1/1   Running

service/oficina-api   ClusterIP   8080/TCP
service/postgres      ClusterIP   5432/TCP

deployment.apps/oficina-api   1/1
statefulset.apps/postgres     1/1

Validar rollout da API:

kubectl rollout status deployment/oficina-api -n oficina --timeout=180s

Resultado esperado:

deployment "oficina-api" successfully rolled out


============================================================
15. VALIDAR LOGS DA API
    ============================================================

Ver logs:

kubectl logs -f deployment/oficina-api -n oficina

Logs esperados:

Tomcat started on port 8080 with context path '/oficina/v1'
Database JDBC URL [jdbc:postgresql://postgres:5432/oficina]
Successfully applied 9 migrations
Started OficinaApplication

Esses logs confirmam:

- API iniciou corretamente
- PostgreSQL foi acessado
- Flyway aplicou migrations
- JPA/Hibernate conectou no banco
- Swagger/OpenAPI iniciou

Para sair dos logs:

CTRL + C


============================================================
16. ACESSAR SWAGGER LOCALMENTE
    ============================================================

O Service oficina-api é do tipo ClusterIP.

Isso significa que ele é acessível internamente no cluster, mas não diretamente no navegador.

Para acessar localmente, usar port-forward:

kubectl port-forward svc/oficina-api 8080:8080 -n oficina

Resultado esperado:

Forwarding from 127.0.0.1:8080 -> 8080
Forwarding from [::1]:8080 -> 8080

Manter esse terminal aberto.

Abrir no navegador:

http://localhost:8080/oficina/v1/swagger-ui.html

OpenAPI JSON:

http://localhost:8080/oficina/v1/api-docs

Se a porta 8080 estiver ocupada, usar 8081:

kubectl port-forward svc/oficina-api 8081:8080 -n oficina

Acessar:

http://localhost:8081/oficina/v1/swagger-ui.html




============================================================
22. LIMPEZA DO AMBIENTE
    ============================================================

Remover recursos Kubernetes:

kubectl delete -f k8s

Destruir cluster kind via Terraform:

terraform -chdir=infra destroy -auto-approve

Confirmar remoção:

kind get clusters

Resultado esperado:

No kind clusters found.

Remover imagem local, opcional:

docker rmi oficina-api:local


============================================================
24. ORDEM RESUMIDA DOS COMANDOS
    ============================================================

Executar na raiz do projeto:

docker compose down

terraform -chdir=infra init
terraform -chdir=infra validate
terraform -chdir=infra plan
terraform -chdir=infra apply -auto-approve

kubectl config use-context kind-techchallenge
kubectl get nodes

docker build -t oficina-api:local .
kind load docker-image oficina-api:local --name techchallenge

kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-configmap.yaml
kubectl apply -f k8s/02-secret.yaml
kubectl apply -f k8s/03-postgres-pvc.yaml
kubectl apply -f k8s/05-postgres-service.yaml
kubectl apply -f k8s/04-postgres-statefulset.yaml

kubectl exec -n oficina postgres-0 -- pg_isready -U postgres -d oficina

kubectl apply -f k8s/06-app-deployment.yaml
kubectl apply -f k8s/07-app-service.yaml
kubectl apply -f k8s/08-hpa.yaml

kubectl get all -n oficina
kubectl rollout status deployment/oficina-api -n oficina --timeout=180s
kubectl logs deployment/oficina-api -n oficina

kubectl port-forward svc/oficina-api 8080:8080 -n oficina

Acessar:

http://localhost:8080/oficina/v1/swagger-ui.html
