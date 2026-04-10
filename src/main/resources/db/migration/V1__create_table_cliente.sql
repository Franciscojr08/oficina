CREATE TABLE cliente (
                         id BIGSERIAL PRIMARY KEY,
                         nome VARCHAR(150) NOT NULL,
                         cpf_cnpj VARCHAR(20) NOT NULL UNIQUE,
                         telefone VARCHAR(20),
                         email VARCHAR(150),
                         ativo BOOLEAN NOT NULL DEFAULT TRUE,
                         data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);