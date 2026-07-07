package br.com.prime.oficina.ordemservico.itens.application.dto;

import java.math.BigDecimal;

public record ItemOrdemServicoResponse(
	String codigoOS,
	Long itemId,
	String itemNome,
	int quantidade,
	BigDecimal valorUnitario,
	BigDecimal valorTotal
) {
}
