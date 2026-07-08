package br.com.prime.oficina.ordemservico.servicos.application.gateway;

import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.servicos.application.StatusServico;
import br.com.prime.oficina.ordemservico.servicos.domain.ServicoOrdemServico;

import java.util.List;
import java.util.Optional;

public interface ServicoOrdemServicoGateway {

	ServicoOrdemServico save(ServicoOrdemServico servicoOrdemServico);

	ServicoOrdemServico saveAndFlush(ServicoOrdemServico servicoOrdemServico);

	Optional<ServicoOrdemServico> findByServicoId(Long servicoId);

	List<ServicoOrdemServico> findByOrdemServicoId(Long ordemServicoId);

	Optional<ServicoOrdemServico> findByOrdemServicoIdAndServicoId(Long ordemServicoId, Long servicoId);

	boolean existsByOrdemServicoIdAndStatusNot(Long ordemServicoId, StatusServico status);

	boolean existsByServicoIdAndOrdemServicoStatusIn(Long itemId, List<StatusOrdemServico> status);

	Double calcularTempoMedioServicosMinutos();
}
