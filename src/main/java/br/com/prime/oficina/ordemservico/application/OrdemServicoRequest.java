package br.com.prime.oficina.ordemservico.application;

import br.com.prime.oficina.ordemservico.itens.application.ItemOrdemServicoRequest;
import br.com.prime.oficina.ordemservico.servicos.application.ServicoOrdemServicoRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
        Long clienteId,

        @NotNull(message = "Id do veiculo é obrigatório")
        Long veiculoId,
        @NotNull(message = "É necessário informar pelo menos um serviço")
        List<ServicoOrdemServicoRequest> servicos,
        List<ItemOrdemServicoRequest> itens
) {
}
