package br.com.prime.oficina.ordemservico.infrastructure;

import br.com.prime.oficina.ordemservico.domain.HistoricoOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoOrdemServicoRepository extends JpaRepository<HistoricoOrdemServico, Long> {

    HistoricoOrdemServico findByOrdemServicoId(Long ordemServicoId);
}
