INSERT INTO usuario (nome, email, senha, role, ativo)
VALUES ('Administrador de Teste', 'admin@oficina.com', 'senha-nao-utilizada-no-token', 'ADMIN', TRUE)
ON CONFLICT (email) DO UPDATE
SET nome = EXCLUDED.nome,
    senha = EXCLUDED.senha,
    role = EXCLUDED.role,
    ativo = EXCLUDED.ativo,
    data_atualizacao = CURRENT_TIMESTAMP;
