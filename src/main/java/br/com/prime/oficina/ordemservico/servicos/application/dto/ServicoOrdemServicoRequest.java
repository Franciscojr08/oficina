package br.com.prime.oficina.ordemservico.servicos.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ServicoOrdemServicoRequest(
		@NotNull(message = "Id Servico é obrigatório")
		@Positive(message = "Id Servico deve ser maior que zero")
		Long servicoId
) {
}
