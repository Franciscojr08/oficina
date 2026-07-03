package br.com.prime.oficina.ordemservico.itens.application;

import jakarta.validation.constraints.NotNull;

public record ItemOrdemServicoRequest(
	@NotNull(message = "Id Item é obrigatório")
	Long itemId,

	@NotNull(message = "Quantidade é obrigatório")
	Integer quantidade
) {
}
