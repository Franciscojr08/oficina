# 🚀 Projeto Oficina API

Projeto backend desenvolvido com **Spring Boot**, utilizando **Docker**
para facilitar a execução do ambiente.

------------------------------------------------------------------------

## 📦 Tecnologias

-   Java 17+
-   Spring Boot
-   Maven (Wrapper incluído)
-   Docker
-   Docker Compose
-   PostgreSQL

------------------------------------------------------------------------

## 📁 Estrutura do Projeto

```text
.
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/prime/oficina/
│   │   │       ├── auth/
│   │   │       │   ├── controller/
│   │   │       │   │   └── AuthController.java
│   │   │       │   ├── gestaoUsuarios/
│   │   │       │   │   ├── application/
│   │   │       │   │   ├── controller/
│   │   │       │   │   ├── domain/
│   │   │       │   │   └── infrastructure/
│   │   │       │   ├── LoginRequest.java
│   │   │       │   └── LoginResponse.java
│   │   │       ├── cliente/
│   │   │       ├── config/
│   │   │       │   ├── OpenApiConfig.java
│   │   │       │   └── SecurityConfig.java
│   │   │       ├── estoque/
│   │   │       ├── item/
│   │   │       ├── movimentoEstoque/
│   │   │       ├── ordemServico/
│   │   │       ├── security/
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   ├── JwtService.java
│   │   │       │   ├── CustomUserDetailsService.java
│   │   │       │   └── domain/
│   │   │       │       └── SecurityUserDetails.java
│   │   │       ├── servico/
│   │   │       ├── shared/
│   │   │       ├── veiculo/
│   │   │       └── OficinaApplication.java
│   │   └── resources/
│   │       ├── db/migration/
│   │       ├── application.properties
│   │       └── application-docker.properties
│   └── test/
├── Dockerfile
├── docker-compose.yml
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md

## 🔐 Autenticação e Segurança

O projeto utiliza autenticação JWT para proteger as rotas administrativas.

### Fluxo de autenticação

1. O usuário realiza login no endpoint `/auth/login`
2. A API valida email e senha
3. A API retorna um token JWT
4. O token deve ser enviado nas próximas requisições no header:

A aplicação cria um usuário administrador inicial via migration do banco.

### Credenciais iniciais

- **Email:** `admin@oficina.com`
- **Senha:** admin

A partir do token retornado da api de login será possível acessar as rotas administrativas.

------------------------------------------------------------------------

## ▶️ Como rodar o projeto

### 🐳 Usando Docker (RECOMENDADO)

> Pré-requisito: Docker e Docker Compose instalados

Criar um arquivo .env na raiz do projeto
``` bash
POSTGRES_DB=oficina
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/oficina
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

SECURITY_JWT_SECRET=jwt-docker-secret-123456789012345678901234567890
SECURITY_JWT_EXPIRATION=7200000
```

1.  Suba os containers:

``` bash
docker-compose up --build -d
```

Para verificar os containers:
``` bash
docker ps
```

Para acompanhar os logs da aplicação:
``` bash
docker logs -f app_oficina
```

------------------------------------------------------------------------
Caso rode local: Suba apenas o banco
``` bash
docker-compose up -d postgres
```


2.  Documentação disponível em:

```{=html}
http://localhost:8080/oficina/v1/swagger-ui/index.html#/
```

3.  Para parar os containers:

``` bash
docker-compose down
```

------------------------------------------------------------------------

## 🗄️ Banco de Dados

O projeto utiliza **PostgreSQL** via Docker.

As configurações estão no:

    docker-compose.yml
