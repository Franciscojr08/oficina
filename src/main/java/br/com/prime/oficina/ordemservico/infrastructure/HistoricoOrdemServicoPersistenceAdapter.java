package br.com.prime.oficina.ordemservico.infrastructure;

import br.com.prime.oficina.ordemservico.application.gateway.HistoricoOrdemServicoGateway;
import br.com.prime.oficina.ordemservico.domain.HistoricoOrdemServico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HistoricoOrdemServicoPersistenceAdapter implements HistoricoOrdemServicoGateway {

	private final HistoricoOrdemServicoRepository historicoOrdemServicoRepository;

	@Override
	public HistoricoOrdemServico save(HistoricoOrdemServico historicoOrdemServico) {
		return historicoOrdemServicoRepository.save(historicoOrdemServico);
	}
}
