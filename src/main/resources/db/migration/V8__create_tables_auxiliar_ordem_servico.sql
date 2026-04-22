CREATE TABLE historico_ordem_servico
(
    id               BIGSERIAL PRIMARY KEY,
    status           VARCHAR(20) NOT NULL,
    observacao       VARCHAR(150) NULL,
    data_cadastro    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NULL,
    ordem_servico_id BIGINT      NOT NULL,
    CONSTRAINT fk_historico_ordem_servico FOREIGN KEY (ordem_servico_id) REFERENCES ordem_servico (id)
);

CREATE TABLE item_ordem_servico
(
    id               BIGSERIAL PRIMARY KEY,
    quantidade       INTEGER        NOT NULL,
    valor_unitario   NUMERIC(10, 2) NOT NULL,
    item_id          BIGINT         NOT NULL,
    ordem_servico_id BIGINT         NOT NULL,
    CONSTRAINT fk_item_os FOREIGN KEY (item_id) REFERENCES item (id),
    CONSTRAINT fk_item_ordem_servico FOREIGN KEY (ordem_servico_id) REFERENCES ordem_servico (id)
);

CREATE TABLE servico_ordem_servico
(
    id               BIGSERIAL PRIMARY KEY,
    status           VARCHAR(10)    NOT NULL,
    valor_unitario   NUMERIC(10, 2) NOT NULL,
    data_cadastro    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_inicio      TIMESTAMP NULL,
    data_fim         TIMESTAMP NULL,
    servico_id       BIGINT         NOT NULL,
    ordem_servico_id BIGINT         NOT NULL,
    CONSTRAINT fk_servico_os FOREIGN KEY (servico_id) REFERENCES servico (id),
    CONSTRAINT fk_servico_ordem_servico FOREIGN KEY (ordem_servico_id) REFERENCES ordem_servico (id)
);