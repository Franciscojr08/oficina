package br.com.prime.oficina.veiculo.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VeiculoRequest(

        @NotNull(message = "Id do cliente é obrigatório")
        Long clienteId,

        @NotBlank(message = "Placa é obrigatória")
        @Size(max = 10, message = "Placa deve ter no máximo 10 caracteres")
        String placa,

        @NotBlank(message = "Marca é obrigatória")
        @Size(max = 80, message = "Marca deve ter no máximo 80 caracteres")
        String marca,

        @NotBlank(message = "Modelo é obrigatório")
        @Size(max = 100, message = "Modelo deve ter no máximo 100 caracteres")
        String modelo,

        @NotNull(message = "Ano é obrigatório")
        @Min(value = 1900, message = "Ano inválido")
        @Max(value = 2100, message = "Ano inválido")
        Integer ano,

        @Size(max = 50, message = "Cor deve ter no máximo 50 caracteres")
        String cor,

        @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
        String observacao
) {
}