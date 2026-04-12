CREATE TABLE veiculo (
                         id BIGSERIAL PRIMARY KEY,
                         cliente_id BIGINT NOT NULL,
                         placa VARCHAR(10) NOT NULL UNIQUE,
                         marca VARCHAR(80) NOT NULL,
                         modelo VARCHAR(100) NOT NULL,
                         ano INTEGER NOT NULL,
                         cor VARCHAR(50),
                         observacao VARCHAR(255),
                         ativo BOOLEAN NOT NULL DEFAULT TRUE,
                         data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_veiculo_cliente
                             FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);