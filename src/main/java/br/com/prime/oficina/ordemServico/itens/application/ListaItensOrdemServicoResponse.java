package br.com.prime.oficina.ordemServico.itens.application;

import java.math.BigDecimal;
import java.util.List;

public record ListaItensOrdemServicoResponse(
	List<ItemOrdemServicoResponse> itens,
	BigDecimal valorTotalItens
) {
}
