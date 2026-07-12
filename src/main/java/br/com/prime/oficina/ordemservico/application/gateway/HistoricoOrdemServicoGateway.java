package br.com.prime.oficina.ordemservico.application.gateway;

import br.com.prime.oficina.ordemservico.domain.HistoricoOrdemServico;

public interface HistoricoOrdemServicoGateway {

	HistoricoOrdemServico save(HistoricoOrdemServico historicoOrdemServico);
}
