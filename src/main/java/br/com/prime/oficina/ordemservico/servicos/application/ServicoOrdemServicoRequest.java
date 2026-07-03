package br.com.prime.oficina.ordemservico.servicos.application;

import jakarta.validation.constraints.NotNull;

public record ServicoOrdemServicoRequest(
		@NotNull(message = "Id Servico é obrigatório")
		Long servicoId
) {
}
