package br.com.prime.oficina.ordemservico.itens.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemOrdemServicoRequest(
	@NotNull(message = "Id Item é obrigatório")
	@Positive(message = "Id Item deve ser maior que zero")
	Long itemId,

	@NotNull(message = "Quantidade é obrigatório")
	@Positive(message = "Quantidade deve ser maior que zero")
	Integer quantidade
) {
}
