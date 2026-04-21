package br.com.prime.oficina.item.application;

import br.com.prime.oficina.item.domain.TipoItem;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ItemAtualizacaoRequest(
		@NotBlank(message = "Nome é obrigatório")
		@Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
		String nome,

		@Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
		String descricao,

		@NotNull(message = "Tipo é obrigatório")
		TipoItem tipo,

		@NotNull(message = "Valor unitário é obrigatório")
		@DecimalMin(value = "0.0", inclusive = false, message = "Valor unitário deve ser maior que zero")
		BigDecimal valorUnitario,

		@NotBlank(message = "Unidade de medida é obrigatória")
		@Size(max = 10, message = "Unidade de medida deve ter no máximo 10 caracteres")
		String unidadeMedida
) {
}
