package br.com.prime.oficina.ordemservico.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrdemServicoRequest(
        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String descricaoProblema,

        @Size(max = 100, message = "Observações deve ter no máximo 100 caracteres")
        String observacoesGerais,

        @NotBlank(message = "Descrição é obrigatório")
        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String descricaoServicosExecutados,

        @NotNull(message = "Id do cliente é obrigatório")
        Long clienteId,

        @NotNull(message = "Id do veiculo é obrigatório")
        Long veiculoId
) {
}
