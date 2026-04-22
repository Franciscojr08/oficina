package br.com.prime.oficina.ordemServico.application;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ItemOrdemServicoRequest(
		@NotBlank(message = "Id Item é obrigatório")
		Long itemId,

        @NotBlank(message = "Quantidade é obrigatório")
        Integer quantidade
) {
}
