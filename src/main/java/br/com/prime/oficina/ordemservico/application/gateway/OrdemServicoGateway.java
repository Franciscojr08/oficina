package br.com.prime.oficina.ordemservico.application.gateway;

import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.domain.OrdemServico;

import java.util.List;
import java.util.Optional;

public interface OrdemServicoGateway {

	OrdemServico save(OrdemServico ordemServico);

	OrdemServico saveAndFlush(OrdemServico ordemServico);

	List<OrdemServico> findByClienteId(Long clienteId);

	Optional<OrdemServico> findByCodigo(String codigo);

	List<OrdemServico> findByStatus(StatusOrdemServico status);

	boolean existsByClienteIdAndStatusIn(Long clienteId, List<StatusOrdemServico> statusOrdemServicos);

	boolean existsByVeiculoIdAndStatusIn(Long veiculoId, List<StatusOrdemServico> statusOrdemServicos);

	Double calcularTempoMedioOSMinutos();

	List<OrdemServico> listagemOrdensServico();

	Optional<OrdemServico> findById(Long id);
}
