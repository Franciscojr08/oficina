package br.com.prime.oficina.ordemservico.servicos.output.persistence;

import br.com.prime.oficina.ordemservico.application.StatusOrdemServico;
import br.com.prime.oficina.ordemservico.servicos.application.StatusServico;
import br.com.prime.oficina.ordemservico.servicos.domain.ServicoOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ServicoOrdemServicoRepository extends JpaRepository<ServicoOrdemServico, Long> {

    Optional<ServicoOrdemServico> findByServicoId(Long servicoId);

	List<ServicoOrdemServico> findByOrdemServicoId(Long ordemServicoId);

	Optional<ServicoOrdemServico> findByOrdemServicoIdAndServicoId(Long ordemServicoId, Long servicoId);

	boolean existsByOrdemServicoIdAndStatusNot(Long ordemServicoId, StatusServico status);

	boolean existsByServicoIdAndOrdemServicoStatusIn(Long itemId,List<StatusOrdemServico> status);

	List<ServicoOrdemServico> findByStatus(StatusServico status);

	@Query(value = """
	SELECT AVG(EXTRACT(EPOCH FROM (data_fim - data_inicio)) / 60)
	FROM servico_ordem_servico
	WHERE status = 'FINALIZADO'
	AND data_inicio IS NOT NULL
	AND data_fim IS NOT NULL
	""", nativeQuery = true)
	Double calcularTempoMedioServicosMinutos();
}
