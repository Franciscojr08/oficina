package br.com.prime.oficina.ordemServico.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OrdemServicoRequest(

        @NotBlank(message = "Código é obrigatório")
        int codigo,

        @Size(max = 255, message = "Descricao deve ter no máximo 255 caracteres")
        String descricaoProblema,

        @Size(max = 100, message = "Observações deve ter no máximo 100 caracteres")
        String observacoesGerais,

        @NotBlank(message = "Descrição é obrigatório")
        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String descricaoServicosExecutados,

        @NotBlank(message = "Status é obrigatório")
        StatusOrdemServico status,

        @NotBlank(message = "Valor total serviços é obrigatório")
        BigDecimal valorTotalServicos,

        @NotBlank(message = "Valor total itens é obrigatorio")
        BigDecimal valorTotalItens,

        @NotNull(message = "Id do cliente é obrigatório")
        Long clienteId,

        @NotNull(message = "Id do veiculo é obrigatório")
        Long veiculoId
) {
}
