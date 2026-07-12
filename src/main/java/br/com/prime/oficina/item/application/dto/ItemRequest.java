package br.com.prime.oficina.item.application.dto;

import br.com.prime.oficina.item.domain.TipoItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ItemRequest(

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
        String unidadeMedida,

        @NotNull(message = "Quantidade inicial é obrigatória")
        @Min(value = 0, message = "Quantidade inicial não pode ser negativa")
        Integer quantidadeInicial,

        @NotNull(message = "Estoque mínimo é obrigatório")
        @Min(value = 0, message = "Estoque mínimo não pode ser negativo")
        Integer estoqueMinimo,

        @Size(max = 150, message = "Observação inicial deve ter no máximo 150 caracteres")
        String observacaoInicial
) {
}