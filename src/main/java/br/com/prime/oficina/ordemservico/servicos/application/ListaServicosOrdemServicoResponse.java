package br.com.prime.oficina.ordemservico.servicos.application;

import java.math.BigDecimal;
import java.util.List;

public record ListaServicosOrdemServicoResponse(
	List<ServicoOrdemServicoResponse> servicos,
	BigDecimal valorTotalServicos
) {
}
