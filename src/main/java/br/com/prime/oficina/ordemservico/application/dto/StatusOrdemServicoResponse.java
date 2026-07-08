package br.com.prime.oficina.ordemservico.application.dto;

import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;

public record StatusOrdemServicoResponse(
	String codigo,
	StatusOrdemServico status
) {
}
