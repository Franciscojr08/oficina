CREATE TABLE usuario (
                         id BIGSERIAL PRIMARY KEY,
                         nome VARCHAR(150) NOT NULL,
                         email VARCHAR(150) NOT NULL UNIQUE,
                         senha VARCHAR(255) NOT NULL,
                         role VARCHAR(30) NOT NULL,
                         ativo BOOLEAN NOT NULL DEFAULT TRUE,
                         data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         data_atualizacao TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
);
