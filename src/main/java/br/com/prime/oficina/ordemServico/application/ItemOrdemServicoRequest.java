package br.com.prime.oficina.ordemServico.application;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ItemOrdemServicoRequest(

        @NotBlank(message = "Quantidade é obrigatório")
        int quantidade,

        @NotBlank(message = "Valor é obrigatorio")
        BigDecimal valorUnitario,

        @NotBlank(message = "Id Item é obrigatório")
        Long itemId
) {
}
