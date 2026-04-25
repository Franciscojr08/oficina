package br.com.prime.oficina.ordemServico.application;

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
