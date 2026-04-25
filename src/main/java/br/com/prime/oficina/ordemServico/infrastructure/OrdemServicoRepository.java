package br.com.prime.oficina.ordemServico.infrastructure;

import br.com.prime.oficina.ordemServico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemServico.domain.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    List<OrdemServico> findByClienteId(Long clienteId);

	List<OrdemServico> findByCodigo(String codigo);

    List<OrdemServico> findByStatus(StatusOrdemServico status);

	boolean existsByCodigo(String codigo);

	boolean existsByClienteIdAndStatusIn(Long clienteId, List<StatusOrdemServico> statusOrdemServicos);

	boolean existsByVeiculoIdAndStatusIn(Long veiculoId, List<StatusOrdemServico> statusOrdemServicos);

	List<OrdemServico> findByStatusIn(List<StatusOrdemServico> status);

	@Query(value = """
		SELECT AVG(EXTRACT(EPOCH FROM (data_fim_execucao - data_inicio_execucao)) / 60)
		FROM ordem_servico
		WHERE status IN ('FINALIZADA', 'ENTREGUE')
		AND data_inicio_execucao IS NOT NULL
		AND data_fim_execucao IS NOT NULL
	""", nativeQuery = true)
	Double calcularTempoMedioOSMinutos();
}
