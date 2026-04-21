CREATE TABLE cliente
(
    id               BIGSERIAL PRIMARY KEY,
    nome             VARCHAR(150) NOT NULL,
    cpf_cnpj         VARCHAR(20)  NOT NULL UNIQUE,
    telefone         VARCHAR(20),
    email            VARCHAR(150),
    cep              VARCHAR(8)   NOT NULL,
    logradouro       VARCHAR(150) NOT NULL,
    bairro           VARCHAR(100) NOT NULL,
    cidade           VARCHAR(100) NOT NULL,
    uf               VARCHAR(2)   NOT NULL,
    data_nascimento  DATE         NOT NULL,
    ativo            BOOLEAN      NOT NULL DEFAULT TRUE,
    data_criacao     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
);