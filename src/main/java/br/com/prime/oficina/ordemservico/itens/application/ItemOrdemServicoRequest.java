package br.com.prime.oficina.ordemservico.itens.application;

import jakarta.validation.constraints.NotBlank;

public record ItemOrdemServicoRequest(
		@NotBlank(message = "Id Item é obrigatório")
		Long itemId,

        @NotBlank(message = "Quantidade é obrigatório")
        Integer quantidade
) {
}
