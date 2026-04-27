package br.com.prime.oficina.apiPublica;


import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;

import java.time.LocalDateTime;

public record AcompanharOrdemServicoPublicaResponse(
        String codigo,
        String descricaoProblema,
        StatusOrdemServico status,
        LocalDateTime dataCadastro,
        LocalDateTime dataInicioExecucao,
        LocalDateTime dataFimExecucao,
        LocalDateTime dataEntregue,
        LocalDateTime dataCancelada
) {
}
