CREATE TABLE movimentacao_estoque
(
    id                BIGSERIAL PRIMARY KEY,
    tipo              VARCHAR(20) NOT NULL,
    quantidade        INTEGER     NOT NULL,
    observacao        VARCHAR(150),
    data_movimentacao TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    item_id           BIGINT      NOT NULL,
    ordem_servico_id  BIGINT,
    CONSTRAINT fk_movimentacao_item FOREIGN KEY (item_id) REFERENCES item (id)
);