package br.com.prime.oficina.ordemservico.servicos.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ListaServicosOrdemServicoResponse(
	List<ServicoOrdemServicoResponse> servicos,
	BigDecimal valorTotalServicos
) {
}
