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
│   │   │       ├── cliente/
│   │   │       │   ├── api/
│   │   │       │   │   └── ClienteController.java
│   │   │       │   ├── application/
│   │   │       │   │   ├── ClienteRequest.java
│   │   │       │   │   ├── ClienteResponse.java
│   │   │       │   │   └── ClienteService.java
│   │   │       │   ├── domain/
│   │   │       │   │   └── Cliente.java
│   │   │       │   └── infrastructure/
│   │   │       │       └── ClienteRepository.java
│   │   │       ├── shared/
│   │   │       │   ├── exception/
│   │   │       │   └── util/
│   │   │       └── OficinaApplication.java
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/
│   │       │       └── V1__create_table_cliente.sql
│   │       └── application.properties
│   └── test/
├── Dockerfile
├── docker-compose.yml
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md

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
