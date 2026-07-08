package br.com.prime.oficina.ordemservico.application.dto;

import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdemServicoResponse(
        Long id,
        String codigo,
        String descricaoProblema,
        String observacoesGerais,
        String descricaoServicosExecutados,
        StatusOrdemServico status,
        BigDecimal valorTotalServicos,
        BigDecimal valorTotalItens,
        LocalDateTime dataCadastro,
        LocalDateTime dataEnvioAprovacao,
        LocalDateTime dataAprovacao,
        LocalDateTime dataInicioExecucao,
        LocalDateTime dataFimExecucao,
        LocalDateTime dataEntregue,
        LocalDateTime dataCancelada
) {
}
