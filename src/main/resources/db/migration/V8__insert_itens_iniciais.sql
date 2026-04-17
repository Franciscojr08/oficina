INSERT INTO item (nome, descricao, tipo, valor_unitario, unidade_medida)
VALUES
    ('Óleo 5W30', 'Óleo sintético para motor', 'INSUMO', 35.00, 'L'),
    ('Filtro de óleo', 'Filtro de óleo automotivo', 'PECA', 25.00, 'UN'),
    ('Pastilha de freio', 'Jogo de pastilhas dianteiras', 'PECA', 120.00, 'JG');

INSERT INTO estoque (quantidade, estoque_minimo, item_id)
VALUES
    (40, 10, 1),
    (20, 5, 2),
    (8, 2, 3);

INSERT INTO movimentacao_estoque (tipo, quantidade, observacao, item_id)
VALUES
    ('ENTRADA', 40, 'Carga inicial do estoque', 1),
    ('ENTRADA', 20, 'Carga inicial do estoque', 2),
    ('ENTRADA', 8, 'Carga inicial do estoque', 3);