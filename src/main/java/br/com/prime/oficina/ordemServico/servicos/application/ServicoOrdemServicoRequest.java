package br.com.prime.oficina.ordemServico.servicos.application;

import jakarta.validation.constraints.NotBlank;

public record ServicoOrdemServicoRequest(
        @NotBlank(message = "Id Servico é obrigatório")
        Long servicoId
) {
}
