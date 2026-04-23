package br.com.prime.oficina.ordemServico.infrastructure;

import br.com.prime.oficina.ordemServico.domain.ServicoOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicoOrdemServicoRepository extends JpaRepository<ServicoOrdemServico, Long> {

    Optional<ServicoOrdemServico> findByServicoId(Long servicoId);

	List<ServicoOrdemServico> findByOrdemServicoId(Long ordemServicoId);
}
