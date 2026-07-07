package br.com.prime.oficina.ordemservico.servicos.infrastructure;

import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.servicos.application.StatusServico;
import br.com.prime.oficina.ordemservico.servicos.application.gateway.ServicoOrdemServicoGateway;
import br.com.prime.oficina.ordemservico.servicos.domain.ServicoOrdemServico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ServicoOrdemServicoPersistenceAdapter implements ServicoOrdemServicoGateway {

	private final ServicoOrdemServicoRepository servicoOrdemServicoRepository;

	@Override
	public ServicoOrdemServico save(ServicoOrdemServico servicoOrdemServico) {
		return servicoOrdemServicoRepository.save(servicoOrdemServico);
	}

	@Override
	public ServicoOrdemServico saveAndFlush(ServicoOrdemServico servicoOrdemServico) {
		return servicoOrdemServicoRepository.saveAndFlush(servicoOrdemServico);
	}

	@Override
	public Optional<ServicoOrdemServico> findByServicoId(Long servicoId) {
		return servicoOrdemServicoRepository.findByServicoId(servicoId);
	}

	@Override
	public List<ServicoOrdemServico> findByOrdemServicoId(Long ordemServicoId) {
		return servicoOrdemServicoRepository.findByOrdemServicoId(ordemServicoId);
	}

	@Override
	public Optional<ServicoOrdemServico> findByOrdemServicoIdAndServicoId(Long ordemServicoId, Long servicoId) {
		return servicoOrdemServicoRepository.findByOrdemServicoIdAndServicoId(ordemServicoId, servicoId);
	}

	@Override
	public boolean existsByOrdemServicoIdAndStatusNot(Long ordemServicoId, StatusServico status) {
		return servicoOrdemServicoRepository.existsByOrdemServicoIdAndStatusNot(ordemServicoId, status);
	}

	@Override
	public boolean existsByServicoIdAndOrdemServicoStatusIn(Long itemId, List<StatusOrdemServico> status) {
		return servicoOrdemServicoRepository.existsByServicoIdAndOrdemServicoStatusIn(itemId, status);
	}

	@Override
	public Double calcularTempoMedioServicosMinutos() {
		return servicoOrdemServicoRepository.calcularTempoMedioServicosMinutos();
	}
}
