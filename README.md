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

1.  Suba os containers:

``` bash
docker-compose up --build -d
```

2.  Documentação disponível em:

```{=html}
http://localhost:8080/swagger-ui.html
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
