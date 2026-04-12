package br.com.prime.oficina.servico.application;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServicoRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String nome,

        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String descricao,

        @NotNull(message = "Preço base é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "Preço base deve ser maior que zero")
        BigDecimal precoBase,

        @NotNull(message = "Tempo estimado é obrigatório")
        @Min(value = 1, message = "Tempo estimado deve ser maior que zero")
        Integer tempoEstimadoMinutos
) {
}