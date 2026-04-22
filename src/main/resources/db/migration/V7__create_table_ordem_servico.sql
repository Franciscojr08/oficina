-- 1. Tabela para controlar o contador anual
CREATE TABLE os_contador (
    ano INTEGER PRIMARY KEY,
    ultimo_valor INTEGER NOT NULL DEFAULT 0
);

-- 2. Função que calcula o próximo código (com reset anual automático)
CREATE OR REPLACE FUNCTION calcular_proximo_codigo_os() RETURNS TEXT AS $$
DECLARE
    ano_atual INTEGER := EXTRACT(YEAR FROM CURRENT_DATE);
    novo_valor INTEGER;
BEGIN
    -- Incrementa o valor do ano atual. Se o ano não existir (virada de ano), cria começando do 1.
    INSERT INTO os_contador (ano, ultimo_valor) 
    VALUES (ano_atual, 1)
    ON CONFLICT (ano) DO UPDATE 
    SET ultimo_valor = os_contador.ultimo_valor + 1
    RETURNING ultimo_valor INTO novo_valor;

    RETURN 'OS-' || ano_atual || '-' || LPAD(novo_valor::text, 6, '0');
END;
$$ LANGUAGE plpgsql;

-- 3. Trigger que garante que o código será calculado
CREATE OR REPLACE FUNCTION trigger_atribuir_codigo_os()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.codigo IS NULL OR NEW.codigo = '' THEN
        NEW.codigo := calcular_proximo_codigo_os();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 4. Tabela Ordem de Serviço
CREATE TABLE ordem_servico
(
    id                            BIGSERIAL PRIMARY KEY,
    codigo                        VARCHAR(20)    NOT NULL UNIQUE,
    descricao_problema            TEXT           NULL,
    observacoes_gerais            VARCHAR(500)   NULL,
    descricao_servicos_executados TEXT           NOT NULL,
    status                        VARCHAR(20)    NOT NULL,
    valor_total_servicos          DECIMAL(10, 2) NOT NULL,
    valor_total_itens             DECIMAL(10, 2) NOT NULL,
    data_cadastro                 TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_envio_aprovacao          TIMESTAMP      NULL,
    data_aprovacao                TIMESTAMP      NULL,
    data_inicio_execucao          TIMESTAMP      NULL,
    data_fim_execucao             TIMESTAMP      NULL,
    data_entregue                 TIMESTAMP      NULL,
    data_cancelada                TIMESTAMP      NULL,
    cliente_id                    BIGINT         NOT NULL,
    veiculo_id                    BIGINT         NOT NULL,
    CONSTRAINT fk_os_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_os_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculo (id)
);

-- 5. Ativação do Trigger
CREATE TRIGGER trigger_os_codigo_insert
BEFORE INSERT ON ordem_servico
FOR EACH ROW EXECUTE FUNCTION trigger_atribuir_codigo_os();

-- 6. Índices otimizados para busca e relatórios
CREATE INDEX idx_os_codigo ON ordem_servico(codigo);
CREATE INDEX idx_os_cliente_status ON ordem_servico(cliente_id, status);
CREATE INDEX idx_os_data_cadastro ON ordem_servico(data_cadastro);