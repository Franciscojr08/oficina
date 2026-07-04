package br.com.prime.oficina.ordemservico.application;

import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrdemServicoRequest(
        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String descricaoProblema,

        @Size(max = 100, message = "Observações deve ter no máximo 100 caracteres")
        String observacoesGerais,

        @NotBlank(message = "Descrição é obrigatório")
        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String descricaoServicosExecutados,

        @NotNull(message = "Id do cliente é obrigatório")
        @Positive(message = "Id do cliente deve ser maior que zero")
        Long clienteId,

        @NotNull(message = "Id do veiculo é obrigatório")
        @Positive(message = "Id do veiculo deve ser maior que zero")
        Long veiculoId,

        @NotEmpty(message = "É necessário informar pelo menos um serviço")
        List<@Positive(message = "Id Servico deve ser maior que zero") Long> servicos,

        List<@Valid ItemOrdemServicoRequest> itens
) {
}
