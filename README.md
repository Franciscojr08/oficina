# Projeto Oficina API

Backend para gestao de oficina mecanica, desenvolvido com Spring Boot e PostgreSQL.
O projeto usa Docker Compose para subir o banco e a aplicacao em ambiente local.

## Tecnologias

- Java 17+
- Spring Boot
- Maven Wrapper
- Docker
- Docker Compose
- PostgreSQL
- Flyway
- Spring Security com JWT
- Springdoc OpenAPI

## Pre-requisitos

- Java 17 ou superior
- Docker e Docker Compose
- Maven opcional, pois o projeto inclui Maven Wrapper (`mvnw` e `mvnw.cmd`)
- PostgreSQL apenas se optar por rodar sem Docker

## Estrutura

```text
.
|-- src/
|   |-- main/
|   |   |-- java/br/com/prime/oficina/
|   |   |   |-- apipublica/
|   |   |   |-- auth/
|   |   |   |-- cliente/
|   |   |   |-- config/
|   |   |   |-- estoque/
|   |   |   |-- item/
|   |   |   |-- movimentoestoque/
|   |   |   |-- ordemservico/
|   |   |   |-- relatorio/
|   |   |   |-- security/
|   |   |   |-- servico/
|   |   |   |-- shared/
|   |   |   `-- veiculo/
|   |   `-- resources/
|   |       |-- db/migration/
|   |       |-- application.properties
|   |       |-- application-docker.properties
|   |       `-- application-test.properties
|   `-- test/
|-- Dockerfile
|-- docker-compose.yml
|-- mvnw
|-- mvnw.cmd
|-- pom.xml
|-- scripts/
|   `-- scripts-iniciais.txt
`-- README.md
```

## Autenticacao e seguranca

As rotas administrativas usam autenticacao JWT.

Fluxo:

1. O usuario faz login em `POST /oficina/v1/auth/login`.
2. A API valida email e senha.
3. A API retorna um token JWT.
4. O token deve ser enviado nas proximas requisicoes:

```http
Authorization: Bearer <token>
```

Rotas publicas ficam em `/oficina/v1/public/**` e nao exigem token.

O usuario administrador inicial e criado pelo script de carga inicial do banco.
Execute `scripts/scripts-iniciais.txt` antes de usar a aplicacao em um banco novo.

### Login

```bash
curl -X POST http://localhost:8080/oficina/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@oficina.com","senha":"admin"}'
```

Resposta esperada:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer"
}
```

Use o token nas rotas administrativas:

```bash
curl http://localhost:8080/oficina/v1/clientes \
  -H "Authorization: Bearer <token>"
```

## Fluxo minimo de uso

Um fluxo basico para operar a oficina pela API:

1. Autenticar em `POST /auth/login`.
2. Cadastrar cliente em `POST /clientes`.
3. Cadastrar veiculo em `POST /veiculos`.
4. Cadastrar servicos em `POST /servicos`.
5. Cadastrar itens/pecas em `POST /itens`.
6. Ajustar estoque de itens em `PUT /estoques/item/{itemId}`.
7. Criar ordem de servico em `POST /ordens`.
8. Adicionar itens em `POST /ordens/{id}/itens`.
9. Adicionar servicos em `POST /ordens/{id}/servicos`.
10. Iniciar diagnostico em `PATCH /ordens/{id}/iniciar-diagnostico`.
11. Solicitar aprovacao em `PATCH /ordens/{id}/solicitar-aprovacao`.
12. Aprovar ou reprovar em `PATCH /ordens/{id}/aprovar` ou `PATCH /ordens/{id}/reprovar`.
13. Iniciar e finalizar servicos em `PATCH /ordens/{id}/servicos/{servicoId}/iniciar` e `PATCH /ordens/{id}/servicos/{servicoId}/finalizar`.
14. Entregar a ordem em `PATCH /ordens/{id}/entregar`.
15. Consultar o andamento publico em `GET /public/ordens/codigo/{codigo}`.

Todos os caminhos acima consideram o prefixo `/oficina/v1`.

## Exemplos rapidos

### Criar cliente

```bash
curl -X POST http://localhost:8080/oficina/v1/clientes \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Joao da Silva",
    "cpfCnpj": "12345678901",
    "telefone": "85999999999",
    "email": "joao@email.com",
    "cep": "60000000",
    "logradouro": "Rua A",
    "bairro": "Centro",
    "cidade": "Fortaleza",
    "uf": "CE",
    "data_nascimento": "1990-01-01"
  }'
```

### Criar ordem de servico

```bash
curl -X POST http://localhost:8080/oficina/v1/ordens \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "descricaoProblema": "Barulho no motor",
    "observacoesGerais": "Cliente relata ruido ao ligar",
    "descricaoServicosExecutados": "Diagnostico inicial",
    "clienteId": 1,
    "veiculoId": 1
  }'
```

### Acompanhar ordem pela API publica

```bash
curl http://localhost:8080/oficina/v1/public/ordens/codigo/OS-2026-0001
```

## Endpoints principais

| Recurso | Endpoints |
| --- | --- |
| Autenticacao | `POST /auth/login` |
| Usuarios | `POST /usuarios` |
| Clientes | `POST /clientes`, `GET /clientes`, `GET /clientes/{id}`, `PUT /clientes/{id}`, `DELETE /clientes/{id}` |
| Veiculos | `POST /veiculos`, `GET /veiculos`, `GET /veiculos/{id}`, `GET /veiculos/cliente/{clienteId}`, `PUT /veiculos/{id}`, `DELETE /veiculos/{id}` |
| Servicos | `POST /servicos`, `GET /servicos`, `GET /servicos/{id}`, `PUT /servicos/{id}`, `DELETE /servicos/{id}` |
| Itens | `POST /itens`, `GET /itens`, `GET /itens/{id}`, `GET /itens/tipo/{tipo}`, `PUT /itens/{id}`, `DELETE /itens/{id}` |
| Estoques | `GET /estoques`, `GET /estoques/item/{itemId}`, `PUT /estoques/item/{itemId}` |
| Movimentacoes de estoque | `GET /movimentacoes-estoque`, `GET /movimentacoes-estoque/item/{itemId}`, `GET /movimentacoes-estoque/item/{itemId}/tipo/{tipo}` |
| Ordens de servico | `POST /ordens`, `GET /ordens`, `GET /ordens/cliente/{id}`, `GET /ordens/codigo/{codigo}`, `GET /ordens/status/{status}`, `PATCH /ordens/{id}/...` |
| Relatorios | `GET /relatorios/ordens-servico/tempo-medio`, `GET /relatorios/ordens-servico/tempo-medio-servicos` |
| API publica | `GET /public/ordens/codigo/{codigo}` |

## Variaveis de ambiente

Crie um arquivo `.env` na raiz do projeto:

```bash
POSTGRES_DB=oficina
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/oficina
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

SECURITY_JWT_SECRET=jwt-docker-secret-123456789012345678901234567890
SECURITY_JWT_EXPIRATION=7200000
```

## Carga inicial do banco

Antes de usar a aplicacao em um banco novo, execute o arquivo:

```text
scripts/scripts-iniciais.txt
```

Esse script insere dados base para demonstracao e uso inicial da API:

- clientes;
- veiculos;
- servicos;
- itens;
- estoque inicial;
- movimentacoes iniciais de estoque;
- usuario administrador.

Credenciais do usuario administrador criado pelo script:

```text
Email: admin@oficina.com
Senha: admin
```

### Executando no banco Docker

Suba os containers para que o PostgreSQL esteja disponivel:

```bash
docker-compose up --build -d
```

Depois execute o script no container do PostgreSQL:

```bash
docker exec -i postgres_oficina psql -U postgres -d oficina < scripts/scripts-iniciais.txt
```

No Windows PowerShell, se o redirecionamento acima nao funcionar corretamente, use:

```powershell
Get-Content scripts\scripts-iniciais.txt | docker exec -i postgres_oficina psql -U postgres -d oficina
```

Execute essa carga apenas uma vez por banco novo. Rodar o script mais de uma vez pode gerar erros de duplicidade em registros que possuem restricoes unicas.

## Como rodar com Docker

Suba banco e aplicacao:

```bash
docker-compose up --build -d
```

Em um banco novo, execute a carga inicial:

```powershell
Get-Content scripts\scripts-iniciais.txt | docker exec -i postgres_oficina psql -U postgres -d oficina
```

Verifique os containers:

```bash
docker ps
```

Acompanhe os logs:

```bash
docker logs -f app_oficina
```

Para parar:

```bash
docker-compose down
```

## Como rodar localmente

Suba apenas o PostgreSQL:

```bash
docker-compose up -d postgres
```

Execute a aplicacao:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
.\mvnw.cmd spring-boot:run
```

## Perfis da aplicacao

- `default`: usa `application.properties`, indicado para execucao local da aplicacao apontando para PostgreSQL em `localhost`.
- `docker`: usa `application-docker.properties`, ativado pelo `docker-compose.yml` com `SPRING_PROFILES_ACTIVE=docker`.
- `test`: usa `application-test.properties`, ativado pelos testes de integracao.

## Documentacao OpenAPI

Swagger UI:

```text
http://localhost:8080/oficina/v1/swagger-ui/index.html#/
```

OpenAPI JSON:

```text
http://localhost:8080/oficina/v1/api-docs
```

A documentacao esta organizada por grupos:

- `autenticacao`
- `gestao-usuarios`
- `cliente`
- `veiculo`
- `servico`
- `item`
- `movimento-estoque`
- `estoque`
- `ordem-servico`
- `relatorio`
- `api-publica`

## Testes

Execute:

```bash
./mvnw test
```

No Windows:

```bash
.\mvnw.cmd test
```

Relatorio de cobertura JaCoCo:

```text
target/site/jacoco/index.html
```

Estado atual da suite: 143 testes passando.

## Relatorios de qualidade e seguranca

Os relatorios gerados para avaliacao do projeto estao disponiveis em `docs/relatorios`:

- [Relatorio JaCoCo](docs/relatorios/jacoco-relatorio.png): evidencia da cobertura de testes.
- [Relatorio Sonar](docs/relatorios/relatoriosonar.pdf): analise estatica de qualidade do codigo.
- [Relatorio OWASP ZAP](<docs/relatorios/ZAP Scanning Report.pdf>): analise dinamica de seguranca da API.

## Decisoes tecnicas

- Organizacao por modulos de dominio, como `cliente`, `veiculo`, `ordemservico`, `estoque` e `auth`.
- Camada `application` para regras de negocio e DTOs de entrada/saida.
- Spring Data JPA para persistencia.
- Flyway para versionamento do banco de dados.
- JWT para autenticacao das rotas administrativas.
- Springdoc OpenAPI para documentacao interativa.
- Testes unitarios e testes de controller com Spring.

## Documentacao DDD

- [Linguagem Ubiqua](docs/ddd/linguagem-ubiqua.md)
- Event Storming de pecas, servicos e ordem de servico: https://miro.com/app/board/uXjVGgeRsuQ=/?share_link_id=397857444241

## CI/CD

O workflow [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml) executa em pushes e pull requests direcionados a `main` ou `master`.

A pipeline usa Java 17 e executa, em ordem:

1. checkout do repositorio;
2. testes Maven contra um PostgreSQL 16 temporario;
3. empacotamento da aplicacao;
4. instalacao das CLIs Terraform, kind e kubectl;
5. `terraform init`, `validate`, `plan` e `apply` em `infra`, criando o cluster kind `techchallenge`;
6. build local da imagem `oficina-api:local`, sem publicacao em registry, e carga direta no kind;
7. aplicacao ordenada dos manifests de namespace, configuracao, PostgreSQL, API e HPA;
8. validacao do PostgreSQL com `pg_isready`, do rollout da API, do PVC e do HPA;
9. chamada de `/oficina/v1/api-docs` por um pod temporario dentro do cluster;
10. coleta de diagnosticos em caso de falha e destruicao do cluster com Terraform em qualquer resultado.

O HPA e criado e sua existencia e validada na pipeline. O metrics-server nao e instalado no GitHub Actions para evitar instabilidade; a coleta real de metricas e o teste visual de escalabilidade do HPA continuam sendo validacoes locais, conforme [documentacao de infraestrutura](infra/README.md). Esta etapa usa apenas Docker, Terraform, kind e Kubernetes locais ao runner: nao usa AWS, EKS, ECS, RDS, ECR ou publicacao da imagem no Docker Hub.

## Solucao de problemas

### Porta 5432 ja esta em uso

Outro PostgreSQL pode estar rodando localmente. Pare o servico local ou altere o mapeamento de porta do servico `postgres` no `docker-compose.yml`.

### Porta 8080 ja esta em uso

Altere `server.port` no `application.properties` ou pare o processo que esta usando a porta.

### Variaveis do `.env` nao foram carregadas

Confirme se o arquivo `.env` esta na raiz do projeto e se os nomes das variaveis batem com o `docker-compose.yml`.

### Erro de autenticacao em banco novo

Execute `scripts/scripts-iniciais.txt` para inserir o usuario administrador inicial.

### Testes falham por conexao com banco

Os testes de integracao usam PostgreSQL conforme `application-test.properties`. Verifique se o banco local esta rodando e acessivel em `localhost:5432`.
