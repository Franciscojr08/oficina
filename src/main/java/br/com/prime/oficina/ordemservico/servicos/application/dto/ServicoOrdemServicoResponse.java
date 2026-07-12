package br.com.prime.oficina.ordemservico.servicos.application.dto;

import br.com.prime.oficina.ordemservico.servicos.application.StatusServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoOrdemServicoResponse(
	String codigoOS,
	Long servicoId,
	String servicoNome,
	BigDecimal valorServico,
	StatusServico status,
	LocalDateTime dataCadastro,
	LocalDateTime dataInicioServico,
	LocalDateTime dataFimServico
) {
}
