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

    .
    ├── .mvn/
    ├── src/
    ├── Dockerfile
    ├── docker-compose.yml
    ├── mvnw
    ├── mvnw.cmd
    ├── pom.xml

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
