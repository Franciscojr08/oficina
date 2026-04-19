package br.com.prime.oficina.ordemServico.infrastructure;

import br.com.prime.oficina.ordemServico.domain.HistoricoOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HistoricoOrdemServicoRepository extends JpaRepository<HistoricoOrdemServico, Long> {

    Optional<HistoricoOrdemServico> findByOrdemServicoId(Long ordemServicoId);
}
