package br.com.prime.oficina.ordemservico.itens.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ListaItensOrdemServicoResponse(
	List<ItemOrdemServicoResponse> itens,
	BigDecimal valorTotalItens
) {
}
