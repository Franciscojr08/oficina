package br.com.prime.oficina.ordemservico.application;

import br.com.prime.oficina.ordemservico.application.dto.*;

import br.com.prime.oficina.ordemservico.domain.HistoricoOrdemServico;
import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import br.com.prime.oficina.ordemservico.application.gateway.HistoricoOrdemServicoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistoricoOrdemServicoService {

	private final HistoricoOrdemServicoGateway historicoOrdemServicoGateway;

	public void registrar(OrdemServico ordemServico, StatusOrdemServico status) {
		HistoricoOrdemServico historicoOrdemServico = new HistoricoOrdemServico();
		historicoOrdemServico.setOrdemServico(ordemServico);
		historicoOrdemServico.setStatus(status);
		historicoOrdemServico.setObservacao(status.getDescricao());
		historicoOrdemServicoGateway.save(historicoOrdemServico);
	}
}
