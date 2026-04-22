CREATE TABLE servico
(
    id               BIGSERIAL PRIMARY KEY,
    nome             VARCHAR(120)   NOT NULL,
    descricao        VARCHAR(255) NULL,
    valor            NUMERIC(10, 2) NOT NULL,
    ativo            BOOLEAN        NOT NULL DEFAULT TRUE,
    data_criacao     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NULL
);