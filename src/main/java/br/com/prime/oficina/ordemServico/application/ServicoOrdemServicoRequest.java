package br.com.prime.oficina.ordemServico.application;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ServicoOrdemServicoRequest(
        @NotBlank(message = "Id Servico é obrigatório")
        Long servicoId
) {
}
