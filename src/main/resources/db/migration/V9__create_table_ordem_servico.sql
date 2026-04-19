CREATE TABLE ordem_servico (
                                      id BIGSERIAL PRIMARY KEY,
                                      codigo INTEGER NOT NULL,
                                      descricao_problema VARCHAR(255),
                                      observacoes_gerais VARCHAR(100),
                                      descricao_servicos_executados VARCHAR(255) NOT NULL,
                                      status VARCHAR(255) NOT NULL,
                                      valor_total_servicos DECIMAL(10,2) NOT NULL,
                                      valor_total_itens DECIMAL(10,2) NOT NULL,
                                      data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      data_envio_aprovacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      data_aprovacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      data_inicio_execucao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      data_finalizada TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      CONSTRAINT fk_cliente_id
                                          FOREIGN KEY (cliente_id) REFERENCES cliente(id),
                                      CONSTRAINT fk_veiculo_id
                                          FOREIGN KEY (veiculo_id) REFERENCES veiculo(id)
);