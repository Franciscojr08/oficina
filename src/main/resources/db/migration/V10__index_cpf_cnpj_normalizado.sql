-- Indice funcional para a consulta da Lambda de autenticacao por CPF (repositorio oficina-lambda).
--
-- A function emite o token consultando o cliente com:
--     WHERE regexp_replace(cpf_cnpj, '\D', '', 'g') = $1
-- porque a coluna cpf_cnpj guarda o documento em formato livre (com ou sem pontuacao). Sem um
-- indice sobre a mesma expressao essa consulta e um seq scan na tabela cliente a cada login.
--
-- A expressao abaixo precisa ser identica, caractere a caractere, a do WHERE da function, senao o
-- planejador do PostgreSQL nao usa o indice.
--
-- CREATE INDEX comum (nao CONCURRENTLY) para rodar dentro da transacao do Flyway; o volume da
-- tabela nao justifica a variante concorrente.
CREATE INDEX idx_cliente_cpf_cnpj_digitos
    ON cliente ((regexp_replace(cpf_cnpj, '\D', '', 'g')));
