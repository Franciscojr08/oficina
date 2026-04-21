CREATE TABLE estoque
(
    id               BIGSERIAL PRIMARY KEY,
    quantidade       INTEGER   NOT NULL,
    estoque_minimo   INTEGER   NOT NULL,
    data_criacao     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    item_id          BIGINT    NOT NULL UNIQUE,
    CONSTRAINT fk_estoque_item FOREIGN KEY (item_id) REFERENCES item (id)
);