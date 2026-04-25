package br.com.prime.oficina.ordemServico.servicos.infrastructure;

import br.com.prime.oficina.ordemServico.servicos.application.StatusServico;
import br.com.prime.oficina.ordemServico.servicos.domain.ServicoOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicoOrdemServicoRepository extends JpaRepository<ServicoOrdemServico, Long> {

    Optional<ServicoOrdemServico> findByServicoId(Long servicoId);

	List<ServicoOrdemServico> findByOrdemServicoId(Long ordemServicoId);

	ServicoOrdemServico findByOrdemServicoIdAndServicoId(Long ordemServicoId, Long servicoId);

	boolean existsByOrdemServicoIdAndStatusNot(Long ordemServicoId, StatusServico status);
}
