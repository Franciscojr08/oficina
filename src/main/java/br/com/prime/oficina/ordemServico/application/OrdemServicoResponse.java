package br.com.prime.oficina.ordemServico.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdemServicoResponse(
        Long id,
        int codigo,
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
        LocalDateTime dataFinalizada
) {
}
