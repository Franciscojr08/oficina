package br.com.prime.oficina.ordemservico.output.persistence;

import br.com.prime.oficina.ordemservico.domain.HistoricoOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoOrdemServicoRepository extends JpaRepository<HistoricoOrdemServico, Long> {

    HistoricoOrdemServico findByOrdemServicoId(Long ordemServicoId);
}
