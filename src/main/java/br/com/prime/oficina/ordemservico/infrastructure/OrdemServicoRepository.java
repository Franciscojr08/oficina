package br.com.prime.oficina.ordemservico.infrastructure;

import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.application.gateway.OrdemServicoGateway;
import br.com.prime.oficina.ordemservico.domain.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long>, OrdemServicoGateway {

    List<OrdemServico> findByClienteId(Long clienteId);

	Optional<OrdemServico> findByCodigo(String codigo);

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

	@Query("""
		SELECT os
		FROM OrdemServico os
		WHERE os.status NOT IN ('FINALIZADA', 'ENTREGUE','CANCELADA')
		ORDER BY
			CASE
				WHEN os.status = 'EM_EXECUCAO' THEN 1
				WHEN os.status = 'AGUARDANDO_APROVACAO' THEN 2
				WHEN os.status = 'EM_DIAGNOSTICO' THEN 3
				WHEN os.status = 'RECEBIDA' THEN 4
				WHEN os.status = 'APROVADA' THEN 5
				WHEN os.status = 'AGUARDANDO_ITENS' THEN 6
				ELSE 99
			END,
			os.dataCadastro ASC
	""")
	List<OrdemServico> listagemOrdensServico();
}
